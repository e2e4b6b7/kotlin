/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.resolve.dfa.TypesRelation.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.AbstractTypeChecker.effectiveVariance
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.types.model.TypeVariance.*
import org.jetbrains.kotlin.utils.addIfNotNull
import java.util.LinkedList
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
    val simpleConstraints = transformToSimpleConstraints(constraints)
    return constraintsClosure(simpleConstraints, typeVariables.toList())
}

private fun constraintsClosure(
    constraints: List<SimpleInferredConstraint>,
    variables: List<ConeTypeVariableType>,
): List<SimpleInferredConstraint> {
    val queue = LinkedList(constraints)
    val visited = mutableSetOf<SimpleInferredConstraint>()
    val simpleConstraints = mutableListOf<SimpleInferredConstraint>()

    while (queue.isNotEmpty()) {
        val constraint = queue.removeFirst()
        if (constraint in visited) continue
        visited.add(constraint)

        if (constraint.subtype in variables) {
            queue
                .filter { it.supertype == constraint.subtype }
                .map { SimpleInferredConstraint(constraint.supertype, it.subtype) }
                .forEach { queue.add(it) }
        }
        if (constraint.supertype in variables) {
            queue
                .filter { it.subtype == constraint.supertype }
                .map { SimpleInferredConstraint(it.supertype, constraint.subtype) }
                .forEach { queue.add(it) }
        }
        if (constraint.supertype !in variables || constraint.subtype !in variables) {
            simpleConstraints.add(constraint)
        }
    }

    return simpleConstraints
}

private fun collectInferredIntersections(flow: PersistentFlow): List<Iterable<ConeKotlinType>> = buildList {
    flow.allVariableTypeStatements.forEach { (variable, typeStatement) ->
        val types = typeStatement.exactType.toMutableList()
        when (val declaration = variable.identifier.symbol.fir) {
            is FirCallableDeclaration -> declaration.returnTypeRef.coneTypeOrNull?.let { types.add(it) }
            is FirClassLikeDeclaration -> {} // todo
            else -> TODO()
        }
        addIfNotNull(types)
    }
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
        collectAllCommonSupertypes(aType, bType).forEach { (a, b) ->
            inferComplexConstraintsFromCommonSupertype(
                a, b, getRelation = { param, aArg, bArg ->
                    val aConst = isConstArgument(aArg)
                    val bConst = isConstArgument(bArg)

                    val aVariance = if (aConst) INV else effectiveVariance(param.getVariance(), aArg.getVariance()) ?: TODO()
                    val bVariance = if (bConst) INV else effectiveVariance(param.getVariance(), bArg.getVariance()) ?: TODO()

                    getArgumentsRelation(aVariance, bVariance)
                }, add = this@buildList::add
            )
        }
    }

private inline fun ConeTypeContext.marked(
    typePrepared: SimpleTypeMarker,
    crossinline newVariable: () -> ConeTypeVariableType,
): SimpleTypeMarker {
    require(typePrepared is ConeKotlinType)
    return if (typePrepared.argumentsCount() == 0) typePrepared
    else typePrepared.withArguments { argument ->
        argument.type?.let { type -> argument.replaceType(type.withChildParameterAttribute) as ConeTypeProjection }
            ?: newVariable()
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

private fun getArgumentsRelation(aVariance: TypeVariance, bVariance: TypeVariance): TypesRelation? = when (aVariance) {
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

private fun ConeTypeContext.transformToSimpleConstraints(constraints: List<ComplexInferredConstraint>): List<SimpleInferredConstraint> {
    val queue = constraints.toMutableList()
    val visited = mutableSetOf<ComplexInferredConstraint>()
    val simpleConstraints = mutableListOf<SimpleInferredConstraint>()

    while (queue.isNotEmpty()) {
        val constraint = queue.removeLast()
        if (constraint in visited) continue
        visited.add(constraint)
        val (newConstraints, newSimpleConstraints) = inferConstraintsFromInferredConstraints(constraint)
        queue.addAll(newConstraints)
        simpleConstraints.addAll(newSimpleConstraints)
    }

    return simpleConstraints
}

private fun ConeTypeContext.inferConstraintsFromInferredConstraints(constraint: ComplexInferredConstraint): Pair<List<ComplexInferredConstraint>, List<SimpleInferredConstraint>> {
    val (relation, aType, bType) = constraint

    require(aType is ConeKotlinType)
    require(bType is ConeKotlinType)

    val complexInferredConstraint = mutableListOf<ComplexInferredConstraint>()
    val simpleInferredConstraint = mutableListOf<SimpleInferredConstraint>()

    if (bType.typeConstructor().isTypeParameterTypeConstructor() || aType.typeConstructor().isTypeParameterTypeConstructor()) {
        with(simpleInferredConstraint) {
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

    collectAllCommonSupertypes(prepared(aType), prepared(bType)).forEach { (a, b) ->
        inferComplexConstraintsFromCommonSupertype(
            a, b, getRelation = { param, aArg, bArg ->
                val aConst = isConstArgument(aArg)
                val bConst = isConstArgument(bArg)

                if (aConst && bConst) EQUAL
                else when (param.getVariance()) {
                    INV -> EQUAL
                    IN -> relation.inverted
                    OUT -> relation
                }
            }, add = complexInferredConstraint::add
        )
    }

    return complexInferredConstraint to simpleInferredConstraint
}

private val TypesRelation.inverted: TypesRelation
    get() = when (this) {
        EQUAL -> EQUAL
        SUBTYPE -> SUPERTYPE
        SUPERTYPE -> SUBTYPE
    }
