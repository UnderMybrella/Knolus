import org.abimon.antlr.knolus.ExampleParser
import org.abimon.antlr.knolus.ExampleParserBaseVisitor
import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.buildFunctionCallAsVarResult
import org.abimon.knolus.transform.*
import org.abimon.knolus.types.*
import org.abimon.kornea.errors.common.KorneaResult


@ExperimentalUnsignedTypes
class ExampleVisitor(val restrictions: KnolusTransVisitorRestrictions<*>, val parser: ExampleParser, val delegate: TransKnolusParserVisitor): ExampleParserBaseVisitor<KorneaResult<KnolusUnion>>() {
    override fun visitScope(ctx: ExampleParser.ScopeContext): KorneaResult<KnolusUnion.ScopeType> = delegate.visitScope(TransScopeBlueprint(ctx))
    override fun visitLine(ctx: ExampleParser.LineContext): KorneaResult<KnolusUnion> = delegate.visitLine(TransLineBlueprint(ctx))
    override fun visitDeclareVariable(ctx: ExampleParser.DeclareVariableContext): KorneaResult<KnolusUnion.DeclareVariableAction> = delegate.visitDeclareVariable(
        TransDeclareVariableBlueprint(
            ctx
        )
    )
    override fun visitSetVariableValue(ctx: ExampleParser.SetVariableValueContext): KorneaResult<KnolusUnion.AssignVariableAction> = delegate.visitSetVariableValue(
        TransAssignVariableBlueprint(
            ctx
        )
    )
    override fun visitDeclareFunction(ctx: ExampleParser.DeclareFunctionContext): KorneaResult<KnolusUnion.FunctionDeclaration> = delegate.visitDeclareFunction(TransDeclareFunctionBlueprint(ctx))
    override fun visitDeclareFunctionBody(ctx: ExampleParser.DeclareFunctionBodyContext): KorneaResult<KnolusUnion.ScopeType> = delegate.visitDeclareFunctionBody(
        TransDeclareFunctionBodyBlueprint(
            ctx
        )
    )
    override fun visitFunctionCall(ctx: ExampleParser.FunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> = delegate.visitFunctionCall(
        TransFunctionCallBlueprint(
            ctx
        )
    )
    override fun visitFunctionCallParameter(ctx: ExampleParser.FunctionCallParameterContext): KorneaResult<KnolusUnion.FunctionParameterType> = delegate.visitFunctionCallParameter(
        TransFunctionCallParameterBlueprint(
            ctx
        )
    )
    override fun visitMemberFunctionCall(ctx: ExampleParser.MemberFunctionCallContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyMemberFunctionCall>> = delegate.visitMemberFunctionCall(
        TransMemberFunctionCallBlueprint(
            ctx
        )
    )
    override fun visitVariableReference(ctx: ExampleParser.VariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusVariableReference>> = delegate.visitVariableReference(
        TransVariableReferenceBlueprint(
            ctx
        )
    )
    override fun visitMemberVariableReference(ctx: ExampleParser.MemberVariableReferenceContext): KorneaResult<KnolusUnion.VariableValue<KnolusPropertyReference>> = delegate.visitMemberVariableReference(
        TransMemberVariableReferenceBlueprint(
            ctx
        )
    )
    override fun visitVariableValue(ctx: ExampleParser.VariableValueContext): KorneaResult<KnolusUnion.VariableValue<KnolusTypedValue>> = delegate.visitVariableValue(
        TransVariableValueBlueprint(
            ctx
        )
    )
    override fun visitArray(ctx: ExampleParser.ArrayContext): KorneaResult<KnolusUnion.VariableValue<KnolusArray<out KnolusTypedValue>>> = delegate.visitArray(TransArrayBlueprint(ctx))
    override fun visitArrayContents(ctx: ExampleParser.ArrayContentsContext): KorneaResult<KnolusUnion.ArrayContents> = delegate.visitArrayContents(TransArrayContentsBlueprint(ctx))
    override fun visitBool(ctx: ExampleParser.BoolContext): KorneaResult<KnolusUnion.VariableValue<KnolusBoolean>> = delegate.visitBool(TransBooleanBlueprint(ctx))
    override fun visitQuotedString(ctx: ExampleParser.QuotedStringContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyString>> = delegate.visitQuotedString(TransQuotedStringBlueprint(ctx))
    override fun visitQuotedCharacter(ctx: ExampleParser.QuotedCharacterContext): KorneaResult<KnolusUnion.VariableValue<KnolusChar>> = delegate.visitQuotedCharacter(
        TransQuotedCharacterBlueprint(
            ctx
        )
    )
    override fun visitNumber(ctx: ExampleParser.NumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusNumericalType>> = delegate.visitNumber(TransNumberBlueprint(ctx))
    override fun visitWholeNumber(ctx: ExampleParser.WholeNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusInt>> = delegate.visitWholeNumber(TransWholeNumberBlueprint(ctx))
    override fun visitDecimalNumber(ctx: ExampleParser.DecimalNumberContext): KorneaResult<KnolusUnion.VariableValue<KnolusDouble>> = delegate.visitDecimalNumber(TransDecimalNumberBlueprint(ctx))
    override fun visitExpression(ctx: ExampleParser.ExpressionContext): KorneaResult<KnolusUnion.VariableValue<KnolusLazyExpression>> = delegate.visitExpression(TransExpressionBlueprint(ctx))

//    override fun visitQuotedStringVariableReference(ctx: ExampleParser.QuotedStringVariableReferenceContext): KorneaResult<KnolusUnion> = delegate.visitQuotedStringVariableReference(TransQuotedStringVariableReferenceBlueprint(ctx))
//    override fun visitExpressionOperation(ctx: ExampleParser.ExpressionOperationContext): KorneaResult<KnolusUnion> = delegate.visitExpressionOperation(TransExpressionOperationBlueprint(ctx))

    override fun visitVersionCommand(ctx: ExampleParser.VersionCommandContext): KorneaResult<KnolusUnion> = buildFunctionCallAsVarResult("version")
}