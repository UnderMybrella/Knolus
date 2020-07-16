package dev.brella.knolus.transform

import org.abimon.antlr.knolus.KnolusParser
import dev.brella.knolus.ExpressionOperator
import dev.brella.knolus.Knolus
import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.types.*
import dev.brella.kornea.annotations.ChangedSince
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.StaticSuccess
import dev.brella.kornea.errors.common.success

@ChangedSince(Knolus.VERSION_1_3_0)
interface KnolusTransVisitorRestrictions<T> {
    @ChangedSince(Knolus.VERSION_1_3_0)
    interface Permissive<T>: KnolusTransVisitorRestrictions<T> {
        companion object: Permissive<StaticSuccess> {
            override fun defaultValue(): KorneaResult<StaticSuccess> = KorneaResult.success()
        }

        fun defaultValue() : KorneaResult<T>

        override fun canVisitScope(context: ScopeBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeScope(context: ScopeBlueprint, scope: KnolusUnion.ScopeType): KorneaResult<T> = defaultValue()

        override fun canVisitVariableDeclaration(context: DeclareVariableBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableDeclaration(context: DeclareVariableBlueprint, decl: KnolusUnion.DeclareVariableAction): KorneaResult<T> = defaultValue()

        override fun canVisitVariableAssignment(context: AssignVariableBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableAssignment(context: AssignVariableBlueprint, variable: KnolusUnion.AssignVariableAction): KorneaResult<T> = defaultValue()

        override fun canVisitVariableValue(context: VariableValueBlueprint): KorneaResult<T> = defaultValue()

        override fun <VT : KnolusTypedValue> shouldTakeVariableValue(context: VariableValueBlueprint, value: KnolusUnion.VariableValue<VT>): KorneaResult<T> = defaultValue()

        override fun canVisitStringValue(context: StringValueBlueprint): KorneaResult<T> = defaultValue()
        override fun <VT : KnolusTypedValue> shouldTakeStringValue(context: StringValueBlueprint, value: KnolusUnion.VariableValue<VT>): KorneaResult<T> = defaultValue()

        override fun canVisitBoolean(context: BooleanBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeBoolean(context: BooleanBlueprint, bool: KnolusUnion.VariableValue<KnolusBoolean>): KorneaResult<T> = defaultValue()

        override fun canVisitQuotedString(context: QuotedStringBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeQuotedString(context: QuotedStringBlueprint, string: KnolusUnion.VariableValue<KnolusLazyString>): KorneaResult<T> = defaultValue()

        override fun canVisitQuotedCharacter(context: QuotedCharacterBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeQuotedCharacter(context: QuotedCharacterBlueprint, char: KnolusUnion.VariableValue<KnolusChar>): KorneaResult<T> = defaultValue()

        override fun canVisitPlainString(context: PlainStringBlueprint): KorneaResult<T> = defaultValue()
        override fun shouldTakePlainString(context: PlainStringBlueprint, string: KnolusUnion.VariableValue<KnolusString>): KorneaResult<T> = defaultValue()

        override fun canVisitVariableReference(context: VariableReferenceBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeVariableReference(context: VariableReferenceBlueprint, ref: KnolusUnion.VariableValue<KnolusVariableReference>): KorneaResult<T> = defaultValue()

        override fun canVisitMemberVariableReference(context: MemberVariableReferenceBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeMemberVariableReference(context: MemberVariableReferenceBlueprint, ref: KnolusUnion.VariableValue<KnolusPropertyReference>): KorneaResult<T> = defaultValue()

        override fun canVisitFunctionCall(context: FunctionCallBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeFunctionCall(context: FunctionCallBlueprint, func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>): KorneaResult<T> = defaultValue()

        override fun canVisitMemberFunctionCall(context: MemberFunctionCallBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeMemberFunctionCall(context: MemberFunctionCallBlueprint, func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>): KorneaResult<T> = defaultValue()

        override fun canVisitFunctionCallParameter(context: FunctionCallParameterBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeFunctionCallParameter(context: FunctionCallParameterBlueprint, param: KnolusUnion.FunctionParameterType): KorneaResult<T> = defaultValue()

        override fun canVisitNumber(context: NumberBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeNumber(context: NumberBlueprint, number: KnolusUnion.VariableValue<KnolusNumericalType>): KorneaResult<T> = defaultValue()

        override fun canVisitWholeNumber(context: WholeNumberBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeWholeNumber(context: WholeNumberBlueprint, int: KnolusUnion.VariableValue<KnolusInt>): KorneaResult<T> = defaultValue()

        override fun canVisitDecimalNumber(context: DecimalNumberBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeDecimalNumber(context: DecimalNumberBlueprint, double: KnolusUnion.VariableValue<KnolusDouble>): KorneaResult<T> = defaultValue()

        override fun canVisitExpression(context: ExpressionBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeExpression(context: ExpressionBlueprint, expression: KnolusUnion.VariableValue<KnolusLazyExpression>): KorneaResult<T> = defaultValue()

        override fun canVisitArray(context: ArrayBlueprint): KorneaResult<T> = defaultValue()

        override fun <FT : KnolusTypedValue> shouldTakeArray(context: ArrayBlueprint, array: KnolusUnion.VariableValue<KnolusArray<FT>>): KorneaResult<T> = defaultValue()

        override fun canVisitArrayContents(context: ArrayContentsBlueprint): KorneaResult<T> = defaultValue()

        override fun shouldTakeArrayContents(context: ArrayContentsBlueprint, array: KnolusUnion.ArrayContents): KorneaResult<T> = defaultValue()
    }
    
    fun canVisitScope(context: ScopeBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeScope(context: ScopeBlueprint, scope: KnolusUnion.ScopeType): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableDeclaration(context: DeclareVariableBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableDeclaration(
        context: DeclareVariableBlueprint,
        decl: KnolusUnion.DeclareVariableAction,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableAssignment(context: AssignVariableBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableAssignment(
        context: AssignVariableBlueprint,
        variable: KnolusUnion.AssignVariableAction,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableValue(context: VariableValueBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun <VT : KnolusTypedValue> shouldTakeVariableValue(
        context: VariableValueBlueprint,
        value: KnolusUnion.VariableValue<VT>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitStringValue(context: StringValueBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun <VT : KnolusTypedValue> shouldTakeStringValue(
        context: StringValueBlueprint,
        value: KnolusUnion.VariableValue<VT>
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitBoolean(context: BooleanBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeBoolean(context: BooleanBlueprint, bool: KnolusUnion.VariableValue<KnolusBoolean>): KorneaResult<T> = KorneaResult.empty()

    fun canVisitQuotedString(context: QuotedStringBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeQuotedString(
        context: QuotedStringBlueprint,
        string: KnolusUnion.VariableValue<KnolusLazyString>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitPlainString(context: PlainStringBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakePlainString(
        context: PlainStringBlueprint,
        string: KnolusUnion.VariableValue<KnolusString>
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitQuotedCharacter(context: QuotedCharacterBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeQuotedCharacter(
        context: QuotedCharacterBlueprint,
        char: KnolusUnion.VariableValue<KnolusChar>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitVariableReference(context: VariableReferenceBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeVariableReference(
        context: VariableReferenceBlueprint,
        ref: KnolusUnion.VariableValue<KnolusVariableReference>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitMemberVariableReference(context: MemberVariableReferenceBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeMemberVariableReference(
        context: MemberVariableReferenceBlueprint,
        ref: KnolusUnion.VariableValue<KnolusPropertyReference>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitFunctionCall(context: FunctionCallBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeFunctionCall(
        context: FunctionCallBlueprint,
        func: KnolusUnion.VariableValue<KnolusLazyFunctionCall>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitMemberFunctionCall(context: MemberFunctionCallBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeMemberFunctionCall(
        context: MemberFunctionCallBlueprint,
        func: KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitFunctionCallParameter(context: FunctionCallParameterBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeFunctionCallParameter(
        context: FunctionCallParameterBlueprint,
        param: KnolusUnion.FunctionParameterType,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitNumber(context: NumberBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeNumber(
        context: NumberBlueprint,
        number: KnolusUnion.VariableValue<KnolusNumericalType>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitWholeNumber(context: WholeNumberBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeWholeNumber(
        context: WholeNumberBlueprint,
        int: KnolusUnion.VariableValue<KnolusInt>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitDecimalNumber(context: DecimalNumberBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeDecimalNumber(
        context: DecimalNumberBlueprint,
        double: KnolusUnion.VariableValue<KnolusDouble>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitExpression(context: ExpressionBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeExpression(
        context: ExpressionBlueprint,
        expression: KnolusUnion.VariableValue<KnolusLazyExpression>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitArray(context: ArrayBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun <FT : KnolusTypedValue> shouldTakeArray(
        context: ArrayBlueprint,
        array: KnolusUnion.VariableValue<KnolusArray<FT>>,
    ): KorneaResult<T> = KorneaResult.empty()

    fun canVisitArrayContents(context: ArrayContentsBlueprint): KorneaResult<T> = KorneaResult.empty()
    fun shouldTakeArrayContents(context: ArrayContentsBlueprint, array: KnolusUnion.ArrayContents): KorneaResult<T> = KorneaResult.empty()
}