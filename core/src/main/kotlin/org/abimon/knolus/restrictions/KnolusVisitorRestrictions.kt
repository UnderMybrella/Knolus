package org.abimon.knolus.restrictions

import org.abimon.antlr.knolus.KnolusParser
import org.abimon.knolus.ExpressionOperator
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.types.*
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.StaticSuccess
import org.abimon.kornea.errors.common.success

interface KnolusVisitorRestrictions<T> {
    interface Permissive<T>: KnolusVisitorRestrictions<T> {
        companion object: Permissive<StaticSuccess> {
            override fun defaultValue(): KorneaResult<StaticSuccess> = KorneaResult.success()
        }

        fun defaultValue() : KorneaResult<T>

        override fun canVisitScope(context: KnolusParser.ScopeContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeScope(context: KnolusParser.ScopeContext, scope: KnolusUnion.ScopeType): KorneaResult<T> = defaultValue()

        override fun canVisitVariableDeclaration(context: KnolusParser.DeclareVariableContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableDeclaration(context: KnolusParser.DeclareVariableContext, decl: KnolusUnion.DeclareVariableAction): KorneaResult<T> = defaultValue()

        override fun canVisitVariableAssignment(context: KnolusParser.SetVariableValueContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableAssignment(context: KnolusParser.SetVariableValueContext, variable: KnolusUnion.AssignVariableAction): KorneaResult<T> = defaultValue()

        override fun canVisitVariableValue(context: KnolusParser.VariableValueContext): KorneaResult<T> = defaultValue()

        override fun <VT : KnolusTypedValue> shouldTakeVariableValue(context: KnolusParser.VariableValueContext, value: KnolusUnion.VariableValue<VT>): KorneaResult<T> = defaultValue()

        override fun canVisitBoolean(context: KnolusParser.BoolContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeBoolean(context: KnolusParser.BoolContext, bool: KnolusUnion.VariableValue<KnolusBoolean>): KorneaResult<T> = defaultValue()

        override fun canVisitQuotedString(context: KnolusParser.QuotedStringContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeQuotedString(context: KnolusParser.QuotedStringContext, string: KnolusUnion.VariableValue<KnolusLazyString>): KorneaResult<T> = defaultValue()

        override fun canVisitQuotedCharacter(context: KnolusParser.QuotedCharacterContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeQuotedCharacter(context: KnolusParser.QuotedCharacterContext, char: KnolusUnion.VariableValue<KnolusChar>): KorneaResult<T> = defaultValue()

        override fun canVisitVariableReference(context: KnolusParser.VariableReferenceContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableReference(context: KnolusParser.VariableReferenceContext, ref: KnolusUnion.VariableValue<KnolusVariableReference>): KorneaResult<T> = defaultValue()

        override fun canVisitMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext, ref: KnolusUnion.VariableValue<KnolusPropertyReference>): KorneaResult<T> = defaultValue()

        override fun canVisitFunctionCall(context: KnolusParser.FunctionCallContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeFunctionCall(context: KnolusParser.FunctionCallContext, func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>): KorneaResult<T> = defaultValue()

        override fun canVisitMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext, func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>): KorneaResult<T> = defaultValue()

        override fun canVisitFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext, param: KnolusUnion.FunctionParameterType): KorneaResult<T> = defaultValue()

        override fun canVisitNumber(context: KnolusParser.NumberContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeNumber(context: KnolusParser.NumberContext, number: KnolusUnion.VariableValue<KnolusNumericalType>): KorneaResult<T> = defaultValue()

        override fun canVisitWholeNumber(context: KnolusParser.WholeNumberContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeWholeNumber(context: KnolusParser.WholeNumberContext, int: KnolusUnion.VariableValue<KnolusInt>): KorneaResult<T> = defaultValue()

        override fun canVisitDecimalNumber(context: KnolusParser.DecimalNumberContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeDecimalNumber(context: KnolusParser.DecimalNumberContext, double: KnolusUnion.VariableValue<KnolusDouble>): KorneaResult<T> = defaultValue()

        override fun canVisitExpression(context: KnolusParser.ExpressionContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeExpression(context: KnolusParser.ExpressionContext, expression: KnolusUnion.VariableValue<KnolusLazyExpression>): KorneaResult<T> = defaultValue()

        override fun canVisitExpressionOperation(context: KnolusParser.ExpressionOperationContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeExpressionOperation(context: KnolusParser.ExpressionOperationContext, operator: ExpressionOperator): KorneaResult<T> = defaultValue()

        override fun canVisitArray(context: KnolusParser.ArrayContext): KorneaResult<T> = defaultValue()

        override fun <FT : KnolusTypedValue> shouldTakeArray(context: KnolusParser.ArrayContext, array: KnolusUnion.VariableValue<KnolusArray<FT>>): KorneaResult<T> = defaultValue()

        override fun canVisitArrayContents(context: KnolusParser.ArrayContentsContext): KorneaResult<T> = defaultValue()

        override fun shouldTakeArrayContents(context: KnolusParser.ArrayContentsContext, array: KnolusUnion.ArrayContents): KorneaResult<T> = defaultValue()
    }
    
    fun canVisitScope(context: KnolusParser.ScopeContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeScope(context: KnolusParser.ScopeContext, scope: KnolusUnion.ScopeType): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableDeclaration(context: KnolusParser.DeclareVariableContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableDeclaration(
        context: KnolusParser.DeclareVariableContext,
        decl: KnolusUnion.DeclareVariableAction,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableAssignment(context: KnolusParser.SetVariableValueContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableAssignment(
        context: KnolusParser.SetVariableValueContext,
        variable: KnolusUnion.AssignVariableAction,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableValue(context: KnolusParser.VariableValueContext): KorneaResult<T> = KorneaResult.empty()
    fun <VT : KnolusTypedValue> shouldTakeVariableValue(
        context: KnolusParser.VariableValueContext,
        value: KnolusUnion.VariableValue<VT>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitBoolean(context: KnolusParser.BoolContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeBoolean(context: KnolusParser.BoolContext, bool: KnolusUnion.VariableValue<KnolusBoolean>): KorneaResult<T> = KorneaResult.empty()

    fun canVisitQuotedString(context: KnolusParser.QuotedStringContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeQuotedString(
        context: KnolusParser.QuotedStringContext,
        string: KnolusUnion.VariableValue<KnolusLazyString>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitQuotedCharacter(context: KnolusParser.QuotedCharacterContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeQuotedCharacter(
        context: KnolusParser.QuotedCharacterContext,
        char: KnolusUnion.VariableValue<KnolusChar>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableReference(context: KnolusParser.VariableReferenceContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableReference(
        context: KnolusParser.VariableReferenceContext,
        ref: KnolusUnion.VariableValue<KnolusVariableReference>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeMemberVariableReference(
        context: KnolusParser.MemberVariableReferenceContext,
        ref: KnolusUnion.VariableValue<KnolusPropertyReference>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitFunctionCall(context: KnolusParser.FunctionCallContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeFunctionCall(
        context: KnolusParser.FunctionCallContext,
        func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeMemberFunctionCall(
        context: KnolusParser.MemberFunctionCallContext,
        func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeFunctionCallParameter(
        context: KnolusParser.FunctionCallParameterContext,
        param: KnolusUnion.FunctionParameterType,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitNumber(context: KnolusParser.NumberContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeNumber(
        context: KnolusParser.NumberContext,
        number: KnolusUnion.VariableValue<KnolusNumericalType>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitWholeNumber(context: KnolusParser.WholeNumberContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeWholeNumber(
        context: KnolusParser.WholeNumberContext,
        int: KnolusUnion.VariableValue<KnolusInt>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitDecimalNumber(context: KnolusParser.DecimalNumberContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeDecimalNumber(
        context: KnolusParser.DecimalNumberContext,
        double: KnolusUnion.VariableValue<KnolusDouble>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitExpression(context: KnolusParser.ExpressionContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeExpression(
        context: KnolusParser.ExpressionContext,
        expression: KnolusUnion.VariableValue<KnolusLazyExpression>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitExpressionOperation(context: KnolusParser.ExpressionOperationContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeExpressionOperation(
        context: KnolusParser.ExpressionOperationContext,
        operator: ExpressionOperator,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitArray(context: KnolusParser.ArrayContext): KorneaResult<T> = KorneaResult.empty()
    fun <FT : KnolusTypedValue> shouldTakeArray(
        context: KnolusParser.ArrayContext,
        array: KnolusUnion.VariableValue<KnolusArray<FT>>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitArrayContents(context: KnolusParser.ArrayContentsContext): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeArrayContents(context: KnolusParser.ArrayContentsContext, array: KnolusUnion.ArrayContents): KorneaResult<T> = KorneaResult.empty()
}