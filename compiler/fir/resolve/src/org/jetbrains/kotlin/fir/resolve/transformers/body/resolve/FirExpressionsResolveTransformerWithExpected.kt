/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.ResolutionMode

class FirExpressionsResolveTransformerWithExpected(transformer: FirAbstractBodyResolveTransformerDispatcher) :
    FirExpressionsResolveTransformer(transformer) {

    private fun FirStatement.coerceIfPossible(data: ResolutionMode) {
        (this@coerceIfPossible as? FirExpression)?.let { localTypeCoercion(it, data) }
    }

    override fun transformExpression(expression: FirExpression, data: ResolutionMode): FirStatement {
        return super.transformExpression(expression, data).apply { coerceIfPossible(data) }.apply { coerceIfPossible(data) }
    }

    override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: ResolutionMode): FirStatement {
        return super.transformSmartCastExpression(smartCastExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformQualifiedAccessExpression(qualifiedAccessExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformPropertyAccessExpression(propertyAccessExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: ResolutionMode): FirStatement {
        return super.transformSafeCallExpression(safeCallExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformCheckedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, data: ResolutionMode): FirStatement {
        return super.transformCheckedSafeCallSubject(checkedSafeCallSubject, data).apply { coerceIfPossible(data) }
    }

    override fun transformFunctionCall(functionCall: FirFunctionCall, data: ResolutionMode): FirStatement {
        return super.transformFunctionCall(functionCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformBlock(block: FirBlock, data: ResolutionMode): FirStatement {
        return super.transformBlock(block, data).apply { coerceIfPossible(data) }
    }

    override fun transformThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression, data: ResolutionMode): FirStatement {
        return super.transformThisReceiverExpression(thisReceiverExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, data: ResolutionMode): FirStatement {
        return super.transformComparisonExpression(comparisonExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformAssignmentOperatorStatement(
        assignmentOperatorStatement: FirAssignmentOperatorStatement,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformAssignmentOperatorStatement(assignmentOperatorStatement, data).apply { coerceIfPossible(data) }
    }

    override fun transformIncrementDecrementExpression(
        incrementDecrementExpression: FirIncrementDecrementExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformIncrementDecrementExpression(incrementDecrementExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: ResolutionMode): FirStatement {
        return super.transformEqualityOperatorCall(equalityOperatorCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: ResolutionMode): FirStatement {
        return super.transformTypeOperatorCall(typeOperatorCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: ResolutionMode): FirStatement {
        return super.transformCheckNotNullCall(checkNotNullCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, data: ResolutionMode): FirStatement {
        return super.transformBinaryLogicExpression(binaryLogicExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformDesugaredAssignmentValueReferenceExpression(
        desugaredAssignmentValueReferenceExpression: FirDesugaredAssignmentValueReferenceExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformDesugaredAssignmentValueReferenceExpression(desugaredAssignmentValueReferenceExpression, data)
            .apply { coerceIfPossible(data) }
    }

    override fun transformVariableAssignment(variableAssignment: FirVariableAssignment, data: ResolutionMode): FirStatement {
        return super.transformVariableAssignment(variableAssignment, data).apply { coerceIfPossible(data) }
    }

    override fun transformCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess, data: ResolutionMode): FirStatement {
        return super.transformCallableReferenceAccess(callableReferenceAccess, data).apply { coerceIfPossible(data) }
    }

    override fun transformGetClassCall(getClassCall: FirGetClassCall, data: ResolutionMode): FirStatement {
        return super.transformGetClassCall(getClassCall, data).apply { coerceIfPossible(data) }
    }

    override fun <T> transformConstExpression(constExpression: FirConstExpression<T>, data: ResolutionMode): FirStatement {
        return super.transformConstExpression(constExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformAnnotation(annotation: FirAnnotation, data: ResolutionMode): FirStatement {
        return super.transformAnnotation(annotation, data).apply { coerceIfPossible(data) }
    }

    override fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: ResolutionMode): FirStatement {
        return super.transformAnnotationCall(annotationCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: ResolutionMode): FirStatement {
        return super.transformErrorAnnotationCall(errorAnnotationCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformDelegatedConstructorCall(
        delegatedConstructorCall: FirDelegatedConstructorCall,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformDelegatedConstructorCall(delegatedConstructorCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformAugmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall, data: ResolutionMode): FirStatement {
        return super.transformAugmentedArraySetCall(augmentedArraySetCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformArrayLiteral(arrayLiteral: FirArrayLiteral, data: ResolutionMode): FirStatement {
        return super.transformArrayLiteral(arrayLiteral, data).apply { coerceIfPossible(data) }
    }

    override fun transformStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, data: ResolutionMode): FirStatement {
        return super.transformStringConcatenationCall(stringConcatenationCall, data).apply { coerceIfPossible(data) }
    }

    override fun transformAnonymousObjectExpression(
        anonymousObjectExpression: FirAnonymousObjectExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformAnonymousObjectExpression(anonymousObjectExpression, data).apply { coerceIfPossible(data) }
    }

    override fun transformAnonymousFunctionExpression(
        anonymousFunctionExpression: FirAnonymousFunctionExpression,
        data: ResolutionMode,
    ): FirStatement {
        return super.transformAnonymousFunctionExpression(anonymousFunctionExpression, data).apply { coerceIfPossible(data) }
    }
}
