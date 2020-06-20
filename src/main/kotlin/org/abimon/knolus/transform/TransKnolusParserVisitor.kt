package org.abimon.knolus.transform

import org.abimon.antlr.knolus.KnolusParser
import org.abimon.kornea.errors.common.KorneaResult
import org.antlr.v4.runtime.tree.ParseTreeVisitor

interface TransKnolusParserVisitor<T> {
    fun visit(ctx: KnolusRuleBlueprint): KorneaResult<T> =
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
    fun visitScope(ctx: KnolusParser.ScopeContext): KorneaResult<T> = visitScope(TransScopeBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.scope].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitScope(ctx: ScopeBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.line].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitLine(ctx: KnolusParser.LineContext): KorneaResult<T> = visitLine(TransLineBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.line].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitLine(ctx: LineBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.declareVariable].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareVariable(ctx: KnolusParser.DeclareVariableContext): KorneaResult<T> = visitDeclareVariable(TransDeclareVariableBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareVariable].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareVariable(ctx: DeclareVariableBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.setVariableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitSetVariableValue(ctx: KnolusParser.SetVariableValueContext): KorneaResult<T> = visitSetVariableValue(TransAssignVariableBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.setVariableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitSetVariableValue(ctx: AssignVariableBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunction].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunction(ctx: KnolusParser.DeclareFunctionContext): KorneaResult<T> = visitDeclareFunction(TransDeclareFunctionBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunction].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunction(ctx: DeclareFunctionBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunctionBody].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunctionBody(ctx: KnolusParser.DeclareFunctionBodyContext): KorneaResult<T> = visitDeclareFunctionBody(TransDeclareFunctionBodyBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.declareFunctionBody].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDeclareFunctionBody(ctx: DeclareFunctionBodyBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.functionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCall(ctx: KnolusParser.FunctionCallContext): KorneaResult<T> = visitFunctionCall(TransFunctionCallBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.functionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCall(ctx: FunctionCallBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.functionCallParameter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCallParameter(ctx: KnolusParser.FunctionCallParameterContext): KorneaResult<T> = visitFunctionCallParameter(TransFunctionCallParameterBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.functionCallParameter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitFunctionCallParameter(ctx: FunctionCallParameterBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.memberFunctionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberFunctionCall(ctx: KnolusParser.MemberFunctionCallContext): KorneaResult<T> = visitMemberFunctionCall(TransMemberFunctionCallBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.memberFunctionCall].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberFunctionCall(ctx: MemberFunctionCallBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.variableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableReference(ctx: KnolusParser.VariableReferenceContext): KorneaResult<T> = visitVariableReference(TransVariableReferenceBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.variableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableReference(ctx: VariableReferenceBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.memberVariableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberVariableReference(ctx: KnolusParser.MemberVariableReferenceContext): KorneaResult<T> = visitMemberVariableReference(TransMemberVariableReferenceBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.memberVariableReference].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitMemberVariableReference(ctx: MemberVariableReferenceBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.variableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableValue(ctx: KnolusParser.VariableValueContext): KorneaResult<T> = visitVariableValue(TransVariableValueBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.variableValue].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitVariableValue(ctx: VariableValueBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.array].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArray(ctx: KnolusParser.ArrayContext): KorneaResult<T> = visitArray(TransArrayBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.array].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArray(ctx: ArrayBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.arrayContents].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArrayContents(ctx: KnolusParser.ArrayContentsContext): KorneaResult<T> = visitArrayContents(TransArrayContentsBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.arrayContents].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitArrayContents(ctx: ArrayContentsBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.bool].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitBool(ctx: KnolusParser.BoolContext): KorneaResult<T> = visitBool(TransBooleanBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.bool].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitBool(ctx: BooleanBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.quotedString].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedString(ctx: KnolusParser.QuotedStringContext): KorneaResult<T> = visitQuotedString(TransQuotedStringBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.quotedString].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedString(ctx: QuotedStringBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.quotedCharacter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedCharacter(ctx: KnolusParser.QuotedCharacterContext): KorneaResult<T> = visitQuotedCharacter(TransQuotedCharacterBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.quotedCharacter].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitQuotedCharacter(ctx: QuotedCharacterBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.number].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitNumber(ctx: KnolusParser.NumberContext): KorneaResult<T> = visitNumber(TransNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.number].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitNumber(ctx: NumberBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.wholeNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitWholeNumber(ctx: KnolusParser.WholeNumberContext): KorneaResult<T> = visitWholeNumber(TransWholeNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.wholeNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitWholeNumber(ctx: WholeNumberBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.decimalNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDecimalNumber(ctx: KnolusParser.DecimalNumberContext): KorneaResult<T> = visitDecimalNumber(TransDecimalNumberBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.decimalNumber].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitDecimalNumber(ctx: DecimalNumberBlueprint): KorneaResult<T>

    /**
     * Visit a parse tree produced by [KnolusParser.expression].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitExpression(ctx: KnolusParser.ExpressionContext): KorneaResult<T> = visitExpression(TransExpressionBlueprint(ctx))

    /**
     * Visit a parse tree produced by [KnolusParser.expression].
     * @param ctx the parse tree
     * @return the visitor result
     */
    fun visitExpression(ctx: ExpressionBlueprint): KorneaResult<T>
}