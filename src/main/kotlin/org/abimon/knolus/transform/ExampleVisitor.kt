package org.abimon.knolus.transform

import org.abimon.antlr.knolus.ExampleParser
import org.abimon.antlr.knolus.ExampleParserBaseVisitor
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusLazyFunctionCall
import org.abimon.kornea.errors.common.KorneaResult


class ExampleVisitor(val restrictions: KnolusTransVisitorRestrictions<*>, val parser: ExampleParser, val delegate: TransKnolusParserVisitor<KnolusUnion>): ExampleParserBaseVisitor<KorneaResult<KnolusUnion>>() {
    override fun visitScope(ctx: ExampleParser.ScopeContext): KorneaResult<KnolusUnion> = delegate.visitScope(TransScopeBlueprint(ctx))
    override fun visitLine(ctx: ExampleParser.LineContext): KorneaResult<KnolusUnion> = delegate.visitLine(TransLineBlueprint(ctx))
    override fun visitDeclareVariable(ctx: ExampleParser.DeclareVariableContext): KorneaResult<KnolusUnion> = delegate.visitDeclareVariable(TransDeclareVariableBlueprint(ctx))
    override fun visitSetVariableValue(ctx: ExampleParser.SetVariableValueContext): KorneaResult<KnolusUnion> = delegate.visitSetVariableValue(TransAssignVariableBlueprint(ctx))
    override fun visitDeclareFunction(ctx: ExampleParser.DeclareFunctionContext): KorneaResult<KnolusUnion> = delegate.visitDeclareFunction(TransDeclareFunctionBlueprint(ctx))
    override fun visitDeclareFunctionBody(ctx: ExampleParser.DeclareFunctionBodyContext): KorneaResult<KnolusUnion> = delegate.visitDeclareFunctionBody(TransDeclareFunctionBodyBlueprint(ctx))
    override fun visitFunctionCall(ctx: ExampleParser.FunctionCallContext): KorneaResult<KnolusUnion> = delegate.visitFunctionCall(TransFunctionCallBlueprint(ctx))
    override fun visitFunctionCallParameter(ctx: ExampleParser.FunctionCallParameterContext): KorneaResult<KnolusUnion> = delegate.visitFunctionCallParameter(TransFunctionCallParameterBlueprint(ctx))
    override fun visitMemberFunctionCall(ctx: ExampleParser.MemberFunctionCallContext): KorneaResult<KnolusUnion> = delegate.visitMemberFunctionCall(TransMemberFunctionCallBlueprint(ctx))
    override fun visitVariableReference(ctx: ExampleParser.VariableReferenceContext): KorneaResult<KnolusUnion> = delegate.visitVariableReference(TransVariableReferenceBlueprint(ctx))
    override fun visitMemberVariableReference(ctx: ExampleParser.MemberVariableReferenceContext): KorneaResult<KnolusUnion> = delegate.visitMemberVariableReference(TransMemberVariableReferenceBlueprint(ctx))
    override fun visitVariableValue(ctx: ExampleParser.VariableValueContext): KorneaResult<KnolusUnion> = delegate.visitVariableValue(TransVariableValueBlueprint(ctx))
    override fun visitArray(ctx: ExampleParser.ArrayContext): KorneaResult<KnolusUnion> = delegate.visitArray(TransArrayBlueprint(ctx))
    override fun visitArrayContents(ctx: ExampleParser.ArrayContentsContext): KorneaResult<KnolusUnion> = delegate.visitArrayContents(TransArrayContentsBlueprint(ctx))
    override fun visitBool(ctx: ExampleParser.BoolContext): KorneaResult<KnolusUnion> = delegate.visitBool(TransBooleanBlueprint(ctx))
    override fun visitQuotedString(ctx: ExampleParser.QuotedStringContext): KorneaResult<KnolusUnion> = delegate.visitQuotedString(TransQuotedStringBlueprint(ctx))
    override fun visitQuotedCharacter(ctx: ExampleParser.QuotedCharacterContext): KorneaResult<KnolusUnion> = delegate.visitQuotedCharacter(TransQuotedCharacterBlueprint(ctx))
    override fun visitNumber(ctx: ExampleParser.NumberContext): KorneaResult<KnolusUnion> = delegate.visitNumber(TransNumberBlueprint(ctx))
    override fun visitWholeNumber(ctx: ExampleParser.WholeNumberContext): KorneaResult<KnolusUnion> = delegate.visitWholeNumber(TransWholeNumberBlueprint(ctx))
    override fun visitDecimalNumber(ctx: ExampleParser.DecimalNumberContext): KorneaResult<KnolusUnion> = delegate.visitDecimalNumber(TransDecimalNumberBlueprint(ctx))
    override fun visitExpression(ctx: ExampleParser.ExpressionContext): KorneaResult<KnolusUnion> = delegate.visitExpression(TransExpressionBlueprint(ctx))

//    override fun visitQuotedStringVariableReference(ctx: ExampleParser.QuotedStringVariableReferenceContext): KorneaResult<KnolusUnion> = delegate.visitQuotedStringVariableReference(TransQuotedStringVariableReferenceBlueprint(ctx))
//    override fun visitExpressionOperation(ctx: ExampleParser.ExpressionOperationContext): KorneaResult<KnolusUnion> = delegate.visitExpressionOperation(TransExpressionOperationBlueprint(ctx))

    override fun visitVersionCommand(ctx: ExampleParser.VersionCommandContext): KorneaResult<KnolusUnion> = KorneaResult.success(KnolusUnion.VariableValue.Lazy(KnolusLazyFunctionCall("version", emptyArray())))
}