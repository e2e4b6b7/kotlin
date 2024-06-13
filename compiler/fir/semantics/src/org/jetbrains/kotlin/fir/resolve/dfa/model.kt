/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import kotlinx.collections.immutable.PersistentSet
import org.jetbrains.kotlin.fir.types.ConeErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// --------------------------------------- Facts ---------------------------------------

data class PersistentTypeStatement(
    override val variable: RealVariable,
    override val exactType: PersistentSet<ConeKotlinType>,
) : VariableTypeStatement()

class MutableTypeStatement(
    override val variable: RealVariable,
    override val exactType: MutableSet<ConeKotlinType> = linkedSetOf(),
) : VariableTypeStatement()

// --------------------------------------- Aliases ---------------------------------------

typealias VariableTypeStatements = Map<RealVariable, VariableTypeStatement>
typealias TemporalTypeStatements = Set<TemporalValueTypeStatement>

abstract class TypeStatements {
    abstract val variableStatements: VariableTypeStatements
    abstract val temporalValueStatements: TemporalTypeStatements
}

data class TypeStatementsBuilder<T : VariableTypeStatement>(
    override val variableStatements: MutableMap<RealVariable, T>,
    override val temporalValueStatements: MutableSet<TemporalValueTypeStatement>,
) : TypeStatements()

fun <T : VariableTypeStatement> newTypeStatementsBuilder(): TypeStatementsBuilder<T> =
    TypeStatementsBuilder(mutableMapOf(), mutableSetOf())

fun newTypeStatements(variableStatements: VariableTypeStatements, temporalStatements: TemporalTypeStatements): TypeStatements =
    object : TypeStatements() {
        override val variableStatements: VariableTypeStatements = variableStatements
        override val temporalValueStatements: TemporalTypeStatements = temporalStatements
    }

fun emptyTypeStatements(): TypeStatements = newTypeStatements(emptyMap(), emptySet())
fun typeStetementsOf(vararg statements: VariableTypeStatement): TypeStatements =
    newTypeStatements(statements.associateBy { it.variable }, emptySet())

fun TypeStatements.isEmpty(): Boolean = variableStatements.isEmpty() && temporalValueStatements.isEmpty()
fun TypeStatements.isNotEmpty(): Boolean = !isEmpty()

// --------------------------------------- DSL ---------------------------------------

infix fun DataFlowVariable.eq(constant: Boolean): OperationStatement =
    OperationStatement(this, if (constant) Operation.EqTrue else Operation.EqFalse)

@Suppress("UNUSED_PARAMETER")
infix fun DataFlowVariable.eq(constant: Nothing?): OperationStatement =
    OperationStatement(this, Operation.EqNull)

@Suppress("UNUSED_PARAMETER")
infix fun DataFlowVariable.notEq(constant: Nothing?): OperationStatement =
    OperationStatement(this, Operation.NotEqNull)

infix fun OperationStatement.implies(effect: Statement): Implication = Implication(this, effect)

infix fun RealVariable.typeEq(type: ConeKotlinType): MutableTypeStatement =
    MutableTypeStatement(this, if (type is ConeErrorType) linkedSetOf() else linkedSetOf(type))

infix fun ConeKotlinType.hasIntersectionWith(other: ConeKotlinType) =
    TemporalValueTypeStatement(setOf(this, other))

// --------------------------------------- Utils ---------------------------------------

@OptIn(ExperimentalContracts::class)
fun DataFlowVariable.isSynthetic(): Boolean {
    contract {
        returns(true) implies (this@isSynthetic is SyntheticVariable)
    }
    return this is SyntheticVariable
}

@OptIn(ExperimentalContracts::class)
fun DataFlowVariable.isReal(): Boolean {
    contract {
        returns(true) implies (this@isReal is RealVariable)
    }
    return this is RealVariable
}
