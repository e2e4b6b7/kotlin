/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.fir.resolve.dfa.TypesRelation.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.AbstractTypeChecker.effectiveVariance
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.types.model.TypeVariance.*
import org.jetbrains.kotlin.utils.keysToMap
import kotlin.reflect.KClass

data class SimpleInferredConstraint(val supertype: KotlinTypeMarker, val subtype: KotlinTypeMarker)

private enum class TypesRelation {
    // A is * of B
    EQUAL, SUBTYPE, SUPERTYPE,

    // Exists type (real type), such that A and B are * of this type
    // Not used as do not allow to infer any constraints
    // BOTH_SUBTYPES, BOTH_SUPERTYPES
}

private data class ComplexInferredConstraint(val relation: TypesRelation, val aType: KotlinTypeMarker, val bType: KotlinTypeMarker)

fun inferredConstraints(flow: PersistentFlow, typeContext: ConeTypeContext): List<SimpleInferredConstraint> = with(typeContext) {
    val intersections = collectInferredIntersections(flow)
    val typeVariables = mutableSetOf<ConeTypeVariableType>()
    val constraints = intersections.flatMap { inferComplexConstraints(it, typeVariables) }
    return transformToSimpleConstraints(constraints, typeVariables)
}

private fun collectInferredIntersections(flow: PersistentFlow): List<Iterable<ConeKotlinType>> = buildList {
//    We can use all intersections for variables from smartcasts instead of collecting them independently
//    To achieve this we have to add original variable type in the list of smartcasted type
//    flow.allVariableTypeStatements.forEach { (_, typeStatement) ->
//        addIfNotNull(typeStatement.exactType)
//    }
    flow.allTypeIntersections.forEach { types ->
        add(types.exactType)
    }
}

private fun ConeTypeContext.inferComplexConstraints(
    intersection: Iterable<ConeKotlinType>,
    typeVariables: MutableCollection<ConeTypeVariableType>,
): List<ComplexInferredConstraint> {
    val types = intersection
        .flatMap { if (it is ConeIntersectionType) it.intersectedTypes else listOf(it) }
        .map(this@inferComplexConstraints::prepared)
        .map { type -> marked(type) { ConeTypeVariable("*").defaultType.also { typeVariables.add(it) } } }
    return buildList {
        for (i in types.indices) {
            for (j in i + 1 until types.size) {
                val aType = types[i]
                val bType = types[j]
                addAll(inferComplexConstraints(aType, bType))
            }
        }
    }
}

private fun ConeTypeContext.inferComplexConstraints(aType: SimpleTypeMarker, bType: SimpleTypeMarker): List<ComplexInferredConstraint> =
    buildList {
        fun haveNoSubtypes(type: SimpleTypeMarker): Boolean =
            type.typeConstructor().isFinalClassOrEnumEntryOrAnnotationClassConstructor() &&
                    type.getArguments().all { it.getVariance() == INV }

        val aFinal = haveNoSubtypes(aType)
        val bFinal = haveNoSubtypes(bType)

        if (aFinal && bFinal) {
            add(ComplexInferredConstraint(EQUAL, aType, bType))
            return@buildList
        }
        if (aFinal) {
            add(ComplexInferredConstraint(SUBTYPE, aType, bType))
            return@buildList
        }
        if (bFinal) {
            add(ComplexInferredConstraint(SUPERTYPE, aType, bType))
            return@buildList
        }
        collectAllCommonSupertypes(aType, bType).forEach { (a, b) ->
            inferComplexConstraintsFromCommonSupertype(
                a, b, getRelation = { param, aArg, bArg ->
                    val aConst = isConstArgument(aArg)
                    val bConst = isConstArgument(bArg)

                    val aVariance = if (aConst) INV else effectiveVariance(param.getVariance(), aArg.getVariance()) ?: TODO()
                    val bVariance = if (bConst) INV else effectiveVariance(param.getVariance(), bArg.getVariance()) ?: TODO()

                    getArgumentsRelationForIntersection(aVariance, bVariance)
                }, add = this@buildList::add
            )
        }
    }

private inline fun ConeTypeContext.marked(
    typePrepared: SimpleTypeMarker,
    crossinline newVariable: () -> ConeTypeProjection,
): SimpleTypeMarker {
    require(typePrepared is ConeKotlinType)
    return if (typePrepared.argumentsCount() == 0) typePrepared
    else typePrepared.withArguments { argument ->
        argument.type?.let { type -> argument.replaceType(type.withChildParameterAttribute) as ConeTypeProjection }
            ?: newVariable()
    }
}

private fun ConeTypeContext.forwardMark(typePrepared: SimpleTypeMarker): SimpleTypeMarker {
    require(typePrepared is ConeKotlinType)
    return if (typePrepared.argumentsCount() == 0 || !typePrepared.attributes.contains(ChildParameter::class)) typePrepared
    else typePrepared.withArguments { argument ->
        argument.type?.let { type -> argument.replaceType(type.withChildParameterAttribute) as ConeTypeProjection }
            ?: argument
    }
}

private fun ConeTypeContext.prepared(type: KotlinTypeMarker): SimpleTypeMarker =
    with(newTypeCheckerState(errorTypesEqualToAnything = false, stubTypesEqualToAnything = false)) {
        return prepareType(refineType(type)).lowerBoundIfFlexible()  // todo: upperBound?
    }

class ChildParameter : ConeAttribute<ChildParameter>() {
    override fun union(other: ChildParameter?): ChildParameter = this
    override fun intersect(other: ChildParameter?): ChildParameter = this
    override fun add(other: ChildParameter?): ChildParameter = this
    override fun isSubtypeOf(other: ChildParameter?): Boolean = true
    override fun toString(): String = "@ChildParameter"
    override val key: KClass<out ChildParameter>
        get() = ChildParameter::class
}

private val ConeKotlinType.withChildParameterAttribute
    get() = withAttributes(attributes + ChildParameter())

private fun ConeTypeContext.collectAllCommonSupertypes(
    aType: SimpleTypeMarker,
    bType: SimpleTypeMarker,
): List<Pair<SimpleTypeMarker, SimpleTypeMarker>> =
    with(newTypeCheckerState(errorTypesEqualToAnything = false, stubTypesEqualToAnything = false)) {
        val aSupertypes = buildMap {
            anySupertype(aType, { false }) { type ->
                put(type.typeConstructor(), type)
                substitutionSupertypePolicy(type)
            }
        }
        buildList {
            anySupertype(bType, { false }) { bSimpleSupertype ->
                when (val aSimpleSupertype = aSupertypes[bSimpleSupertype.typeConstructor()]) {
                    null -> substitutionSupertypePolicy(bSimpleSupertype)
                    else -> {
                        add(aSimpleSupertype to bSimpleSupertype)
                        TypeCheckerState.SupertypesPolicy.None
                    }
                }
            }
        }
    }

private inline fun ConeTypeContext.inferComplexConstraintsFromCommonSupertype(
    a: SimpleTypeMarker,
    b: SimpleTypeMarker,
    getRelation: (param: TypeParameterMarker, aArg: TypeArgumentMarker, bArg: TypeArgumentMarker) -> TypesRelation?,
    add: (ComplexInferredConstraint) -> Unit,
) {
    require(a.typeConstructor() == b.typeConstructor())
    val typeConstructor = a.typeConstructor()

    val argumentsCount = a.argumentsCount()
    require(argumentsCount == b.argumentsCount())
    require(argumentsCount == typeConstructor.parametersCount())

    for (i in 0 until argumentsCount) {
        val aArg = a.getArgument(i)
        val bArg = b.getArgument(i)
        val param = typeConstructor.getParameter(i)

        if (aArg.isStarProjection() || bArg.isStarProjection()) continue // todo: convert types to any?

        getRelation(param, aArg, bArg)?.let { relation ->
            add(ComplexInferredConstraint(relation, aArg.getType(), bArg.getType()))
        }
    }
}

private fun ConeTypeContext.isConstArgument(arg: TypeArgumentMarker): Boolean {
    if (arg.isStarProjection()) return true
    return !(arg.getType() as ConeKotlinType).contains { it.attributes.contains(ChildParameter::class) }
}

private fun getArgumentsRelationForIntersection(aVariance: TypeVariance, bVariance: TypeVariance): TypesRelation? =
    when (aVariance) {
        INV -> when (bVariance) {
            INV -> EQUAL
            IN -> SUPERTYPE
            OUT -> SUBTYPE
        }
        IN -> when (bVariance) {
            INV -> SUBTYPE
            IN -> null
            OUT -> SUBTYPE
        }
        OUT -> when (bVariance) {
            INV -> SUPERTYPE
            IN -> SUPERTYPE
            OUT -> null
        }
    }

private fun getArgumentsRelationForSubtyping(
    typesRelation: TypesRelation,
    aVariance: TypeVariance,
    bVariance: TypeVariance,
): TypesRelation? =
    when (aVariance) {
        INV -> when (bVariance) {
            INV -> EQUAL
            IN -> typesRelation.inverted
            OUT -> typesRelation
        }
        IN -> when (bVariance) {
            INV -> typesRelation.inverted
            IN -> typesRelation.inverted
            OUT -> null
        }
        OUT -> when (bVariance) {
            INV -> typesRelation
            IN -> null
            OUT -> typesRelation
        }
    }

private class VariableBounds {
    // todo: use set & do not process duplicates
    // todo: if we found first equal constraint, we can add only constraints to that type
    private val lowerBounds = mutableListOf<KotlinTypeMarker>()
    private val upperBounds = mutableListOf<KotlinTypeMarker>()

    fun addNewBound(
        relation: TypesRelation,
        type: KotlinTypeMarker,
    ): List<ComplexInferredConstraint> {
        val newConstraints = mutableListOf<ComplexInferredConstraint>()

        when (relation) {
            EQUAL -> {
                newConstraints.addAll(lowerBounds.map { ComplexInferredConstraint(SUPERTYPE, type, it) })
                newConstraints.addAll(upperBounds.map { ComplexInferredConstraint(SUBTYPE, type, it) })
                lowerBounds.add(type)
                upperBounds.add(type)
            }
            SUBTYPE -> {
                newConstraints.addAll(upperBounds.map { ComplexInferredConstraint(SUBTYPE, type, it) })
                lowerBounds.add(type)
            }
            SUPERTYPE -> {
                newConstraints.addAll(lowerBounds.map { ComplexInferredConstraint(SUPERTYPE, type, it) })
                upperBounds.add(type)
            }
        }

        return newConstraints
    }
}

private fun ConeTypeContext.transformToSimpleConstraints(
    constraints: List<ComplexInferredConstraint>,
    typeVariables: MutableSet<ConeTypeVariableType>,
): List<SimpleInferredConstraint> {
    val queue = constraints.toMutableList()
    val visited = mutableSetOf<ComplexInferredConstraint>()
    val simpleConstraints = mutableListOf<SimpleInferredConstraint>()
    val variablesBounds = typeVariables.keysToMap { VariableBounds() }

    while (queue.isNotEmpty()) {
        val constraint = queue.removeLast()
        if (constraint in visited) continue
        visited.add(constraint)
        val (newConstraints, newSimpleConstraints) = inferConstraintsFromInferredConstraints(constraint, variablesBounds)
        queue.addAll(newConstraints)
        simpleConstraints.addAll(newSimpleConstraints)
    }

    return simpleConstraints
}

private fun ConeTypeContext.inferConstraintsFromInferredConstraints(
    constraint: ComplexInferredConstraint,
    variablesBounds: Map<ConeTypeVariableType, VariableBounds>,
): Pair<List<ComplexInferredConstraint>, List<SimpleInferredConstraint>> {
    val (relation, aType, bType) = constraint

    require(aType is ConeKotlinType)
    require(bType is ConeKotlinType)

    val complexInferredConstraints = mutableListOf<ComplexInferredConstraint>()
    val simpleInferredConstraints = mutableListOf<SimpleInferredConstraint>()

    if (aType in variablesBounds || bType in variablesBounds) {
        variablesBounds[aType]?.let { bounds ->
            complexInferredConstraints.addAll(bounds.addNewBound(relation.inverted, bType))
        }
        variablesBounds[bType]?.let { bounds ->
            complexInferredConstraints.addAll(bounds.addNewBound(relation, aType))
        }
        return complexInferredConstraints to simpleInferredConstraints
    }

    if (bType.typeConstructor().isTypeParameterTypeConstructor() || aType.typeConstructor().isTypeParameterTypeConstructor()) {
        with(simpleInferredConstraints) {
            when (relation) {
                EQUAL -> {
                    add(SimpleInferredConstraint(aType, bType))
                    add(SimpleInferredConstraint(bType, aType))
                }
                SUBTYPE -> add(SimpleInferredConstraint(bType, aType))
                SUPERTYPE -> add(SimpleInferredConstraint(aType, bType))
            }
        }
    }

    collectAllCommonSupertypes(forwardMark(prepared(aType)), forwardMark(prepared(bType))).forEach { (a, b) ->
        inferComplexConstraintsFromCommonSupertype(
            a, b, getRelation = { param, aArg, bArg ->
                val aConst = isConstArgument(aArg)
                val bConst = isConstArgument(bArg)

                val aVariance = if (aConst) INV else effectiveVariance(param.getVariance(), aArg.getVariance()) ?: TODO()
                val bVariance = if (bConst) INV else effectiveVariance(param.getVariance(), bArg.getVariance()) ?: TODO()

                getArgumentsRelationForSubtyping(relation, aVariance, bVariance) ?: TODO("unsat")
            }, add = complexInferredConstraints::add
        )
    }

    return complexInferredConstraints to simpleInferredConstraints
}

private val TypesRelation.inverted: TypesRelation
    get() = when (this) {
        EQUAL -> EQUAL
        SUBTYPE -> SUPERTYPE
        SUPERTYPE -> SUBTYPE
    }
