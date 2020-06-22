package org.abimon.knolus.transform

import org.abimon.antlr.knolus.KnolusParser
import org.abimon.knolus.Knolus
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.types.*
import org.abimon.kornea.annotations.AvailableSince
import org.abimon.kornea.errors.common.KorneaResult
import org.antlr.v4.runtime.tree.ParseTreeVisitor

@AvailableSince(Knolus.VERSION_1_2_0)
interface TransKnolusParserVisitor {
    fun visit(ctx: KnolusRuleBlueprint): KorneaResult<KnolusUnion> =
        when (ctx) {
            is ScopeBlueprint -> visitScope(ctx)
            is LineBlueprint -> visitLine(ctx)
            is DeclareVariableBlueprint -> visitDeclareVariable(ctx)
            is AssignVariableBlueprint -> visitSetVariableValue(ctx)
            is DeclareFunctionBlueprint -> visitDeclareFunction(ctx)
            is DeclareFunctionBodyBlueprint -> visitDeclareFunctionBody(ctx)
            is FunctionCallBlueprint -> visitFunctionCall(ctx)
            is FunctionCallParameterBlueprint -> visitFunctionCallParameter(ctx)
            is MemberFunctionCallBlueprint -> visitMemberFunctionCall(ctx)
            is VariableReferenceBlueprint -> visitVariableReference(ctx)
            is MemberVariableReferenceBlueprint -> visitMemberVariableReference(ctx)
            is VariableValueBlueprint -> visitVariableValue(ctx)
            is ArrayBlueprint -> visitArray(ctx)
            is ArrayContentsBlueprint -> visitArrayContents(ctx)
            is BooleanBlueprint -> visitBool(ctx)
            is QuotedStringBlueprint -> visitQuotedString(ctx)
            is QuotedCharacterBlueprint -> visitQuotedCharacter(ctx)
            is NumberBlueprint -> visitNumber(ctx)
            is WholeNumberBlueprint -> visitWholeNumber(ctx)
            is DecimalNumberBlueprint -> visitDecimalNumber(ctx)
            is ExpressionBlueprint -> visitExpression(ctx)
            else -> KorneaResult.empty()
        }
    
    /**
     * Visit a parse tree produced by [KnolusParser.scope].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitScope(ctx: KnolusParser.ScopeContext): KorneaResult<KnolusUnion.ScopeType> = visitScope(TransScopeBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.scope].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitScope(ctx: ScopeBlueprint): KorneaResult<KnolusUnion.ScopeType>

    /**
     * Visit a parse tree produced by [KnolusParser.line].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitLine(ctx: KnolusParser.LineContext): KorneaResult<KnolusUnion> = visitLine(TransLineBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.line].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitLine(ctx: LineBlueprint): KorneaResult<KnolusUnion>

    /**
     * Visit a parse tree produced by [KnolusParser.declareVariable].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareVariable(ctx: KnolusParser.DeclareVariableContext): KorneaResult<KnolusUnion.DeclareVariableAction> = visitDeclareVariable(TransDeclareVariableBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareVariable].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareVariable(ctx: DeclareVariableBlueprint): KorneaResult<KnolusUnion.DeclareVariableAction>

    /**
     * Visit a parse tree produced by [KnolusParser.setVariableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitSetVariableValue(ctx: KnolusParser.SetVariableValueContext): KorneaResult<KnolusUnion.AssignVariableAction> = visitSetVariableValue(TransAssignVariableBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.setVariableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitSetVariableValue(ctx: AssignVariableBlueprint): KorneaResult<KnolusUnion.AssignVariableAction>

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunction].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunction(ctx: KnolusParser.DeclareFunctionContext): KorneaResult<KnolusUnion.FunctionDeclaration> = visitDeclareFunction(TransDeclareFunctionBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunction].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunction(ctx: DeclareFunctionBlueprint): KorneaResult<KnolusUnion.FunctionDeclaration>

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunctionBody].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunctionBody(ctx: KnolusParser.DeclareFunctionBodyContext): KorneaResult<KnolusUnion.ScopeType> = visitDeclareFunctionBody(TransDeclareFunctionBodyBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunctionBody].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunctionBody(ctx: DeclareFunctionBodyBlueprint): KorneaResult<KnolusUnion.ScopeType>

    /**
     * Visit a parse tree produced by [KnolusParser.functionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> = visitFunctionCall(TransFunctionCallBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.functionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCall(ctx: FunctionCallBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>>

    /**
     * Visit a parse tree produced by [KnolusParser.functionCallParameter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KorneaResult<KnolusUnion.FunctionParameterType> = visitFunctionCallParameter(TransFunctionCallParameterBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.functionCallParameter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCallParameter(ctx: FunctionCallParameterBlueprint): KorneaResult<KnolusUnion.FunctionParameterType>

    /**
     * Visit a parse tree produced by [KnolusParser.memberFunctionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> = visitMemberFunctionCall(TransMemberFunctionCallBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.memberFunctionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberFunctionCall(ctx: MemberFunctionCallBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>>

    /**
     * Visit a parse tree produced by [KnolusParser.variableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>> = visitVariableReference(TransVariableReferenceBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.variableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableReference(ctx: VariableReferenceBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>>

    /**
     * Visit a parse tree produced by [KnolusParser.memberVariableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>> = visitMemberVariableReference(TransMemberVariableReferenceBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.memberVariableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberVariableReference(ctx: MemberVariableReferenceBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>>

    /**
     * Visit a parse tree produced by [KnolusParser.variableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableValue(ctx: KnolusParser.VariableValueContext): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> = visitVariableValue(TransVariableValueBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.variableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableValue(ctx: VariableValueBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>>

    /**
     * Visit a parse tree produced by [KnolusParser.array].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArray(ctx: KnolusParser.ArrayContext): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> = visitArray(TransArrayBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.array].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArray(ctx: ArrayBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>>

    /**
     * Visit a parse tree produced by [KnolusParser.arrayContents].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArrayContents(ctx: KnolusParser.ArrayContentsContext): KorneaResult<KnolusUnion.ArrayContents> = visitArrayContents(TransArrayContentsBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.arrayContents].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArrayContents(ctx: ArrayContentsBlueprint): KorneaResult<KnolusUnion.ArrayContents>

    /**
     * Visit a parse tree produced by [KnolusParser.bool].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitBool(ctx: KnolusParser.BoolContext): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>> = visitBool(TransBooleanBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.bool].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitBool(ctx: BooleanBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>>

    /**
     * Visit a parse tree produced by [KnolusParser.quotedString].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>> = visitQuotedString(TransQuotedStringBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.quotedString].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedString(ctx: QuotedStringBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>>

    /**
     * Visit a parse tree produced by [KnolusParser.quotedCharacter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedCharacter(ctx: KnolusParser.QuotedCharacterContext): KorneaResult<KnolusUnion.VariableValue<KnolusChar>> = visitQuotedCharacter(TransQuotedCharacterBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.quotedCharacter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedCharacter(ctx: QuotedCharacterBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusChar>>

    /**
     * Visit a parse tree produced by [KnolusParser.number].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitNumber(ctx: KnolusParser.NumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> = visitNumber(TransNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.number].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitNumber(ctx: NumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>>

    /**
     * Visit a parse tree produced by [KnolusParser.wholeNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitWholeNumber(ctx: KnolusParser.WholeNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusInt>> = visitWholeNumber(TransWholeNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.wholeNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitWholeNumber(ctx: WholeNumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusInt>>

    /**
     * Visit a parse tree produced by [KnolusParser.decimalNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDecimalNumber(ctx: KnolusParser.DecimalNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>> = visitDecimalNumber(TransDecimalNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.decimalNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDecimalNumber(ctx: DecimalNumberBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>>

    /**
     * Visit a parse tree produced by [KnolusParser.expression].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitExpression(ctx: KnolusParser.ExpressionContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>> = visitExpression(TransExpressionBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.expression].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitExpression(ctx: ExpressionBlueprint): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>>
}