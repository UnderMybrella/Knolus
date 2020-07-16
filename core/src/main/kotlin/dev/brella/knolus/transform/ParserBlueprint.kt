@file:Suppress("UNCHECKED_CAST")

package dev.brella.knolus.transform

import dev.brella.knolus.ExpressionOperator
import dev.brella.knolus.Knolus
import dev.brella.kornea.annotations.AvailableSince
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.reflect.Modifier

@AvailableSince(Knolus.VERSION_1_2_0)
interface ParserBlueprint<R : ParserRuleContext> {
    val scopeContext: Class<out R>
    val lineContext: Class<out R>
    val declareVariableContext: Class<out R>
    val setVariableValueContext: Class<out R>
    val declareFunctionContext: Class<out R>
    val declareFunctionBodyContext: Class<out R>
    val functionCallContext: Class<out R>
    val functionCallParameterContext: Class<out R>
    val memberFunctionCallContext: Class<out R>
    val variableReferenceContext: Class<out R>
    val memberVariableReferenceContext: Class<out R>
    val variableValueContext: Class<out R>
    val stringValueContext: Class<out R>
    val arrayContext: Class<out R>
    val arrayContentsContext: Class<out R>
    val boolContext: Class<out R>
    val quotedStringContext: Class<out R>
    val plainStringContext: Class<out R>
    val quotedCharacterContext: Class<out R>
    val quotedStringVariableReferenceContext: Class<out R>
    val numberContext: Class<out R>
    val wholeNumberContext: Class<out R>
    val decimalNumberContext: Class<out R>
    val expressionContext: Class<out R>
    val expressionOperationContext: Class<out R>

    val tokenTypeToRaw: Map<KnolusTokenBlueprint.TokenType, Int>
    val rawToTokenType: Map<Int, KnolusTokenBlueprint.TokenType>

    fun getVariableNameFromDeclareVariableContext(ctx: R): Token
    fun getVariableNameFromAssignVariableContext(ctx: R): Token

    fun getFunctionNameFromDeclareFunctionContext(ctx: R): Token
    fun getParametersFromDeclareFunctionContext(ctx: R): List<Token>

    fun getFunctionNameFromFunctionCallContext(ctx: R): Token
    fun getFunctionParametersFromFunctionCallContext(ctx: R): List<R>

    fun getParameterNameFromFunctionCallParameterContext(ctx: R): Token?

    fun getMemberNameFromMemberFunctionCallContext(ctx: R): Token

    fun getVariableNameFromVariableReferenceContext(ctx: R): Token

    fun getMemberNameFromMemberVariableReferenceContext(ctx: R): Token

    fun getStartingValueFromExpressionContext(ctx: R): ParserRuleContext
    fun getExpressionOperationsFromExpressionContext(ctx: R): List<ParserRuleContext>
    fun getExpressionValuesFromExpressionContext(ctx: R): List<ParserRuleContext>

    fun parseScope(): ScopeBlueprint
}

fun ParserBlueprint<ParserRuleContext>.contextToOperator(parserRuleContext: ParserRuleContext): ExpressionOperator =
    when ((parserRuleContext.children.firstOrNull { tree -> tree is TerminalNode } as? TerminalNode)?.symbol?.type?.let(rawToTokenType::get)) {
        KnolusTokenBlueprint.TokenType.EXPR_PLUS -> ExpressionOperator.PLUS
        KnolusTokenBlueprint.TokenType.EXPR_MINUS -> ExpressionOperator.MINUS
        KnolusTokenBlueprint.TokenType.EXPR_MULTIPLY -> ExpressionOperator.MULTIPLY
        KnolusTokenBlueprint.TokenType.EXPR_DIVIDE -> ExpressionOperator.DIVIDE
        KnolusTokenBlueprint.TokenType.EXPR_EXPONENTIAL -> ExpressionOperator.EXPONENTIAL
        else -> throw IllegalStateException("Invalid token in $parserRuleContext")
    }

@AvailableSince(Knolus.VERSION_1_2_0)
class ReflectiveParserBlueprint<R : ParserRuleContext, P : Parser> private constructor(val parser: P) : ParserBlueprint<R> {
    companion object {
        operator fun <P : Parser> invoke(parser: P): ReflectiveParserBlueprint<ParserRuleContext, P> = ReflectiveParserBlueprint(parser)
    }

    private val parserClass by lazy { parser::class.java }
    private val contextClasses by lazy {
        parserClass.declaredClasses
            .filterIsInstance<Class<out R>>()
            .associateBy(Class<*>::getSimpleName)
    }

    override val scopeContext: Class<out R> by lazy { contextClasses.getValue("ScopeContext") }
    override val lineContext: Class<out R> by lazy { contextClasses.getValue("LineContext") }
    override val declareVariableContext: Class<out R> by lazy { contextClasses.getValue("DeclareVariableContext") }
    override val setVariableValueContext: Class<out R> by lazy { contextClasses.getValue("SetVariableValueContext") }
    override val declareFunctionContext: Class<out R> by lazy { contextClasses.getValue("DeclareFunctionContext") }
    override val declareFunctionBodyContext: Class<out R> by lazy { contextClasses.getValue("DeclareFunctionBodyContext") }
    override val functionCallContext: Class<out R> by lazy { contextClasses.getValue("FunctionCallContext") }
    override val functionCallParameterContext: Class<out R> by lazy { contextClasses.getValue("FunctionCallParameterContext") }
    override val memberFunctionCallContext: Class<out R> by lazy { contextClasses.getValue("MemberFunctionCallContext") }
    override val variableReferenceContext: Class<out R> by lazy { contextClasses.getValue("VariableReferenceContext") }
    override val memberVariableReferenceContext: Class<out R> by lazy { contextClasses.getValue("MemberVariableReferenceContext") }
    override val variableValueContext: Class<out R> by lazy { contextClasses.getValue("VariableValueContext") }
    override val stringValueContext: Class<out R> by lazy { contextClasses.getValue("StringValueContext") }
    override val arrayContext: Class<out R> by lazy { contextClasses.getValue("ArrayContext") }
    override val arrayContentsContext: Class<out R> by lazy { contextClasses.getValue("ArrayContentsContext") }
    override val boolContext: Class<out R> by lazy { contextClasses.getValue("BoolContext") }
    override val quotedStringContext: Class<out R> by lazy { contextClasses.getValue("QuotedStringContext") }
    override val plainStringContext: Class<out R> by lazy { contextClasses.getValue("PlainStringContext") }
    override val quotedCharacterContext: Class<out R> by lazy { contextClasses.getValue("QuotedCharacterContext") }
    override val quotedStringVariableReferenceContext: Class<out R> by lazy { contextClasses.getValue("QuotedStringVariableReferenceContext") }
    override val numberContext: Class<out R> by lazy { contextClasses.getValue("NumberContext") }
    override val wholeNumberContext: Class<out R> by lazy { contextClasses.getValue("WholeNumberContext") }
    override val decimalNumberContext: Class<out R> by lazy { contextClasses.getValue("DecimalNumberContext") }
    override val expressionContext: Class<out R> by lazy { contextClasses.getValue("ExpressionContext") }
    override val expressionOperationContext: Class<out R> by lazy { contextClasses.getValue("ExpressionOperationContext") }

    @ExperimentalStdlibApi
    override val tokenTypeToRaw: Map<KnolusTokenBlueprint.TokenType, Int> by lazy {
        val values = KnolusTokenBlueprint.TokenType.values()
        val fields = parserClass.declaredFields

        values.associateWith { type ->
            fields.first { field ->
                field.name == type.name &&
                field.type == Int::class.javaPrimitiveType &&
                Modifier.isPublic(field.modifiers) &&
                Modifier.isStatic(field.modifiers) &&
                Modifier.isFinal(field.modifiers)
            }.getInt(null)
        }
    }

    @ExperimentalStdlibApi
    override val rawToTokenType: Map<Int, KnolusTokenBlueprint.TokenType> by lazy { tokenTypeToRaw.entries.associate { (k, v) -> Pair(v, k) } }

    private val variableNameFieldInDeclareVariableContext: TField<R, Token> by field(this::declareVariableContext, "variableName")
    private val variableNameFieldInAssignVariableContext: TField<R, Token> by field(this::setVariableValueContext, "variableName")

    private val functionNameFieldInDeclareFunctionContext: TField<R, Token> by field(this::declareFunctionContext, "functionName")
    private val parametersFieldInDeclareFunctionContext: TField<R, List<Token>> by field(this::declareFunctionContext, "parameters")

    private val functionNameFieldInFunctionCallContext: TField<R, Token> by field(this::functionCallContext, "functionName")
    private val functionParametersFieldInFunctionCallContext: TField<R, List<R>> by field(this::functionCallContext, "parameters")

    private val parameterNameFieldInFunctionCallParameterContext: TField<R, Token?> by field(this::functionCallParameterContext, "parameterName")

    private val memberNameFieldInMemberFunctionCallContext: TField<R, Token> by field(this::memberFunctionCallContext, "memberName")

    private val variableNameFieldInVariableReferenceContext: TField<R, Token> by field(this::variableReferenceContext, "variableName")
    private val memberNameFieldInMemberVariableReferenceContext: TField<R, Token> by field(this::memberVariableReferenceContext, "memberName")

    private val startingValueFieldInExpressionContext: TField<R, ParserRuleContext> by field(this::expressionContext, "startingValue")
    private val operationsInExpressionContext: TField<R, List<ParserRuleContext>> by field(this::expressionContext, "exprOps")
    private val valuesInExpressionContext: TField<R, List<ParserRuleContext>> by field(this::expressionContext, "exprVals")

    private val parseScopeMethod by lazy { parserClass.getMethod("scope") }

    override fun getVariableNameFromDeclareVariableContext(ctx: R): Token = variableNameFieldInDeclareVariableContext[ctx]
    override fun getVariableNameFromAssignVariableContext(ctx: R): Token = variableNameFieldInAssignVariableContext[ctx]

    override fun getFunctionNameFromDeclareFunctionContext(ctx: R): Token = functionNameFieldInDeclareFunctionContext[ctx]
    override fun getParametersFromDeclareFunctionContext(ctx: R): List<Token> = parametersFieldInDeclareFunctionContext[ctx]


    override fun getFunctionNameFromFunctionCallContext(ctx: R): Token = functionNameFieldInFunctionCallContext[ctx]
    override fun getFunctionParametersFromFunctionCallContext(ctx: R): List<R> = functionParametersFieldInFunctionCallContext[ctx]

    override fun getParameterNameFromFunctionCallParameterContext(ctx: R): Token? = parameterNameFieldInFunctionCallParameterContext[ctx]

    override fun getMemberNameFromMemberFunctionCallContext(ctx: R): Token = memberNameFieldInMemberFunctionCallContext[ctx]

    override fun getVariableNameFromVariableReferenceContext(ctx: R): Token = variableNameFieldInVariableReferenceContext[ctx]

    override fun getMemberNameFromMemberVariableReferenceContext(ctx: R): Token = memberNameFieldInMemberVariableReferenceContext[ctx]

    override fun getStartingValueFromExpressionContext(ctx: R): ParserRuleContext = startingValueFieldInExpressionContext[ctx]
    override fun getExpressionOperationsFromExpressionContext(ctx: R): List<ParserRuleContext> = operationsInExpressionContext[ctx]
    override fun getExpressionValuesFromExpressionContext(ctx: R): List<ParserRuleContext> = valuesInExpressionContext[ctx]

    override fun parseScope(): ScopeBlueprint = TransScopeBlueprint(scopeContext.cast(parseScopeMethod(parser)))
}