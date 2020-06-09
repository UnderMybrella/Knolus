package org.abimon.knolus

import org.abimon.antlr.knolus.KnolusParser
import org.abimon.knolus.types.*

interface KnolusVisitorRestrictions {
    object Permissive : KnolusVisitorRestrictions {
        override fun canVisitScope(context: KnolusParser.ScopeContext): Boolean = true

        override fun shouldTakeScope(context: KnolusParser.ScopeContext, scope: KnolusUnion.ScopeType): Boolean = true

        override fun canVisitVariableDeclaration(context: KnolusParser.DeclareVariableContext): Boolean = true

        override fun shouldTakeVariableDeclaration(
            context: KnolusParser.DeclareVariableContext,
            decl: KnolusUnion.DeclareVariableAction,
        ): Boolean = true

        override fun canVisitVariableAssignment(context: KnolusParser.SetVariableValueContext): Boolean = true

        override fun shouldTakeVariableAssignment(
            context: KnolusParser.SetVariableValueContext,
            variable: KnolusUnion.AssignVariableAction,
        ): Boolean = true

        override fun canVisitVariableValue(context: KnolusParser.VariableValueContext): Boolean = true

        override fun <T : KnolusTypedValue> shouldTakeVariableValue(
            context: KnolusParser.VariableValueContext,
            value: KnolusUnion.VariableValue<T>,
        ): Boolean = true

        override fun canVisitBoolean(context: KnolusParser.BoolContext): Boolean = true

        override fun shouldTakeBoolean(
            context: KnolusParser.BoolContext,
            bool: KnolusUnion.VariableValue<KnolusBoolean>,
        ): Boolean = true

        override fun canVisitQuotedString(context: KnolusParser.QuotedStringContext): Boolean = true

        override fun shouldTakeQuotedString(
            context: KnolusParser.QuotedStringContext,
            string: KnolusUnion.VariableValue<KnolusLazyString>,
        ): Boolean = true

        override fun canVisitQuotedCharacter(context: KnolusParser.QuotedCharacterContext): Boolean = true

        override fun shouldTakeQuotedCharacter(
            context: KnolusParser.QuotedCharacterContext,
            char: KnolusUnion.VariableValue<KnolusChar>,
        ): Boolean = true

        override fun canVisitVariableReference(context: KnolusParser.VariableReferenceContext): Boolean = true

        override fun shouldTakeVariableReference(
            context: KnolusParser.VariableReferenceContext,
            ref: KnolusUnion.VariableValue<KnolusVariableReference>,
        ): Boolean = true

        override fun canVisitMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext): Boolean =
            true

        override fun shouldTakeMemberVariableReference(
            context: KnolusParser.MemberVariableReferenceContext,
            ref: KnolusUnion.VariableValue<KnolusPropertyReference>,
        ): Boolean = true

        override fun canVisitFunctionCall(context: KnolusParser.FunctionCallContext): Boolean = true

        override fun shouldTakeFunctionCall(
            context: KnolusParser.FunctionCallContext,
            func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>,
        ): Boolean = true

        override fun canVisitMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext): Boolean = true

        override fun shouldTakeMemberFunctionCall(
            context: KnolusParser.MemberFunctionCallContext,
            func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>,
        ): Boolean = true

        override fun canVisitFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext): Boolean = true

        override fun shouldTakeFunctionCallParameter(
            context: KnolusParser.FunctionCallParameterContext,
            param: KnolusUnion.FunctionParameterType,
        ): Boolean = true

        override fun canVisitNumber(context: KnolusParser.NumberContext): Boolean = true

        override fun shouldTakeNumber(
            context: KnolusParser.NumberContext,
            number: KnolusUnion.VariableValue<KnolusNumericalType>,
        ): Boolean = true

        override fun canVisitWholeNumber(context: KnolusParser.WholeNumberContext): Boolean = true

        override fun shouldTakeWholeNumber(
            context: KnolusParser.WholeNumberContext,
            int: KnolusUnion.VariableValue<KnolusInt>,
        ): Boolean = true

        override fun canVisitDecimalNumber(context: KnolusParser.DecimalNumberContext): Boolean = true

        override fun shouldTakeDecimalNumber(
            context: KnolusParser.DecimalNumberContext,
            double: KnolusUnion.VariableValue<KnolusDouble>,
        ): Boolean = true

        override fun canVisitExpression(context: KnolusParser.ExpressionContext): Boolean = true

        override fun shouldTakeExpression(
            context: KnolusParser.ExpressionContext,
            expression: KnolusUnion.VariableValue<KnolusLazyExpression>,
        ): Boolean = true

        override fun canVisitExpressionOperation(context: KnolusParser.ExpressionOperationContext): Boolean = true

        override fun shouldTakeExpressionOperation(
            context: KnolusParser.ExpressionOperationContext,
            operator: ExpressionOperator,
        ): Boolean = true

        override fun canVisitArray(context: KnolusParser.ArrayContext): Boolean = true

        override fun <T : KnolusTypedValue> shouldTakeArray(
            context: KnolusParser.ArrayContext,
            array: KnolusUnion.VariableValue<KnolusArray<T>>,
        ): Boolean = true

        override fun canVisitArrayContents(context: KnolusParser.ArrayContentsContext): Boolean = true

        override fun shouldTakeArrayContents(
            context: KnolusParser.ArrayContentsContext,
            array: KnolusUnion.ArrayContents,
        ): Boolean = true
    }
    
    object Lockdown: KnolusVisitorRestrictions {
        override fun canVisitScope(context: KnolusParser.ScopeContext): Boolean = false

        override fun shouldTakeScope(context: KnolusParser.ScopeContext, scope: KnolusUnion.ScopeType): Boolean = false

        override fun canVisitVariableDeclaration(context: KnolusParser.DeclareVariableContext): Boolean = false

        override fun shouldTakeVariableDeclaration(
            context: KnolusParser.DeclareVariableContext,
            decl: KnolusUnion.DeclareVariableAction,
        ): Boolean = false

        override fun canVisitVariableAssignment(context: KnolusParser.SetVariableValueContext): Boolean = false

        override fun shouldTakeVariableAssignment(
            context: KnolusParser.SetVariableValueContext,
            variable: KnolusUnion.AssignVariableAction,
        ): Boolean = false

        override fun canVisitVariableValue(context: KnolusParser.VariableValueContext): Boolean = false

        override fun <T : KnolusTypedValue> shouldTakeVariableValue(
            context: KnolusParser.VariableValueContext,
            value: KnolusUnion.VariableValue<T>,
        ): Boolean = false

        override fun canVisitBoolean(context: KnolusParser.BoolContext): Boolean = false

        override fun shouldTakeBoolean(
            context: KnolusParser.BoolContext,
            bool: KnolusUnion.VariableValue<KnolusBoolean>,
        ): Boolean = false

        override fun canVisitQuotedString(context: KnolusParser.QuotedStringContext): Boolean = false

        override fun shouldTakeQuotedString(
            context: KnolusParser.QuotedStringContext,
            string: KnolusUnion.VariableValue<KnolusLazyString>,
        ): Boolean = false

        override fun canVisitQuotedCharacter(context: KnolusParser.QuotedCharacterContext): Boolean = false

        override fun shouldTakeQuotedCharacter(
            context: KnolusParser.QuotedCharacterContext,
            char: KnolusUnion.VariableValue<KnolusChar>,
        ): Boolean = false

        override fun canVisitVariableReference(context: KnolusParser.VariableReferenceContext): Boolean = false

        override fun shouldTakeVariableReference(
            context: KnolusParser.VariableReferenceContext,
            ref: KnolusUnion.VariableValue<KnolusVariableReference>,
        ): Boolean = false

        override fun canVisitMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext): Boolean =
            true

        override fun shouldTakeMemberVariableReference(
            context: KnolusParser.MemberVariableReferenceContext,
            ref: KnolusUnion.VariableValue<KnolusPropertyReference>,
        ): Boolean = false

        override fun canVisitFunctionCall(context: KnolusParser.FunctionCallContext): Boolean = false

        override fun shouldTakeFunctionCall(
            context: KnolusParser.FunctionCallContext,
            func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>,
        ): Boolean = false

        override fun canVisitMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext): Boolean = false

        override fun shouldTakeMemberFunctionCall(
            context: KnolusParser.MemberFunctionCallContext,
            func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>,
        ): Boolean = false

        override fun canVisitFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext): Boolean = false

        override fun shouldTakeFunctionCallParameter(
            context: KnolusParser.FunctionCallParameterContext,
            param: KnolusUnion.FunctionParameterType,
        ): Boolean = false

        override fun canVisitNumber(context: KnolusParser.NumberContext): Boolean = false

        override fun shouldTakeNumber(
            context: KnolusParser.NumberContext,
            number: KnolusUnion.VariableValue<KnolusNumericalType>,
        ): Boolean = false

        override fun canVisitWholeNumber(context: KnolusParser.WholeNumberContext): Boolean = false

        override fun shouldTakeWholeNumber(
            context: KnolusParser.WholeNumberContext,
            int: KnolusUnion.VariableValue<KnolusInt>,
        ): Boolean = false

        override fun canVisitDecimalNumber(context: KnolusParser.DecimalNumberContext): Boolean = false

        override fun shouldTakeDecimalNumber(
            context: KnolusParser.DecimalNumberContext,
            double: KnolusUnion.VariableValue<KnolusDouble>,
        ): Boolean = false

        override fun canVisitExpression(context: KnolusParser.ExpressionContext): Boolean = false

        override fun shouldTakeExpression(
            context: KnolusParser.ExpressionContext,
            expression: KnolusUnion.VariableValue<KnolusLazyExpression>,
        ): Boolean = false

        override fun canVisitExpressionOperation(context: KnolusParser.ExpressionOperationContext): Boolean = false

        override fun shouldTakeExpressionOperation(
            context: KnolusParser.ExpressionOperationContext,
            operator: ExpressionOperator,
        ): Boolean = false

        override fun canVisitArray(context: KnolusParser.ArrayContext): Boolean = false

        override fun <T : KnolusTypedValue> shouldTakeArray(
            context: KnolusParser.ArrayContext,
            array: KnolusUnion.VariableValue<KnolusArray<T>>,
        ): Boolean = false

        override fun canVisitArrayContents(context: KnolusParser.ArrayContentsContext): Boolean = false

        override fun shouldTakeArrayContents(
            context: KnolusParser.ArrayContentsContext,
            array: KnolusUnion.ArrayContents,
        ): Boolean = false
    }

    fun canVisitScope(context: KnolusParser.ScopeContext): Boolean
    fun shouldTakeScope(context: KnolusParser.ScopeContext, scope: KnolusUnion.ScopeType): Boolean

    fun canVisitVariableDeclaration(context: KnolusParser.DeclareVariableContext): Boolean
    fun shouldTakeVariableDeclaration(
        context: KnolusParser.DeclareVariableContext,
        decl: KnolusUnion.DeclareVariableAction,
    ): Boolean

    fun canVisitVariableAssignment(context: KnolusParser.SetVariableValueContext): Boolean
    fun shouldTakeVariableAssignment(
        context: KnolusParser.SetVariableValueContext,
        variable: KnolusUnion.AssignVariableAction,
    ): Boolean

    fun canVisitVariableValue(context: KnolusParser.VariableValueContext): Boolean
    fun <T : KnolusTypedValue> shouldTakeVariableValue(
        context: KnolusParser.VariableValueContext,
        value: KnolusUnion.VariableValue<T>,
    ): Boolean

    fun canVisitBoolean(context: KnolusParser.BoolContext): Boolean
    fun shouldTakeBoolean(context: KnolusParser.BoolContext, bool: KnolusUnion.VariableValue<KnolusBoolean>): Boolean

    fun canVisitQuotedString(context: KnolusParser.QuotedStringContext): Boolean
    fun shouldTakeQuotedString(
        context: KnolusParser.QuotedStringContext,
        string: KnolusUnion.VariableValue<KnolusLazyString>,
    ): Boolean

    fun canVisitQuotedCharacter(context: KnolusParser.QuotedCharacterContext): Boolean
    fun shouldTakeQuotedCharacter(
        context: KnolusParser.QuotedCharacterContext,
        char: KnolusUnion.VariableValue<KnolusChar>,
    ): Boolean

    fun canVisitVariableReference(context: KnolusParser.VariableReferenceContext): Boolean
    fun shouldTakeVariableReference(
        context: KnolusParser.VariableReferenceContext,
        ref: KnolusUnion.VariableValue<KnolusVariableReference>,
    ): Boolean

    fun canVisitMemberVariableReference(context: KnolusParser.MemberVariableReferenceContext): Boolean
    fun shouldTakeMemberVariableReference(
        context: KnolusParser.MemberVariableReferenceContext,
        ref: KnolusUnion.VariableValue<KnolusPropertyReference>,
    ): Boolean

    fun canVisitFunctionCall(context: KnolusParser.FunctionCallContext): Boolean
    fun shouldTakeFunctionCall(
        context: KnolusParser.FunctionCallContext,
        func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>,
    ): Boolean

    fun canVisitMemberFunctionCall(context: KnolusParser.MemberFunctionCallContext): Boolean
    fun shouldTakeMemberFunctionCall(
        context: KnolusParser.MemberFunctionCallContext,
        func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>,
    ): Boolean

    fun canVisitFunctionCallParameter(context: KnolusParser.FunctionCallParameterContext): Boolean
    fun shouldTakeFunctionCallParameter(
        context: KnolusParser.FunctionCallParameterContext,
        param: KnolusUnion.FunctionParameterType,
    ): Boolean

    fun canVisitNumber(context: KnolusParser.NumberContext): Boolean
    fun shouldTakeNumber(
        context: KnolusParser.NumberContext,
        number: KnolusUnion.VariableValue<KnolusNumericalType>,
    ): Boolean

    fun canVisitWholeNumber(context: KnolusParser.WholeNumberContext): Boolean
    fun shouldTakeWholeNumber(
        context: KnolusParser.WholeNumberContext,
        int: KnolusUnion.VariableValue<KnolusInt>,
    ): Boolean

    fun canVisitDecimalNumber(context: KnolusParser.DecimalNumberContext): Boolean
    fun shouldTakeDecimalNumber(
        context: KnolusParser.DecimalNumberContext,
        double: KnolusUnion.VariableValue<KnolusDouble>,
    ): Boolean

    fun canVisitExpression(context: KnolusParser.ExpressionContext): Boolean
    fun shouldTakeExpression(
        context: KnolusParser.ExpressionContext,
        expression: KnolusUnion.VariableValue<KnolusLazyExpression>,
    ): Boolean

    fun canVisitExpressionOperation(context: KnolusParser.ExpressionOperationContext): Boolean
    fun shouldTakeExpressionOperation(
        context: KnolusParser.ExpressionOperationContext,
        operator: ExpressionOperator,
    ): Boolean

    fun canVisitArray(context: KnolusParser.ArrayContext): Boolean
    fun <T : KnolusTypedValue> shouldTakeArray(
        context: KnolusParser.ArrayContext,
        array: KnolusUnion.VariableValue<KnolusArray<T>>,
    ): Boolean

    fun canVisitArrayContents(context: KnolusParser.ArrayContentsContext): Boolean
    fun shouldTakeArrayContents(context: KnolusParser.ArrayContentsContext, array: KnolusUnion.ArrayContents): Boolean
}