package dev.brella.knolus.transform

import dev.brella.knolus.ExpressionOperator
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

interface KnolusBlueprint
interface KnolusRuleBlueprint : KnolusBlueprint {
    fun matchingText(): String
    fun toString(recognizer: Recognizer<*, *>): String
}
interface TransRuleBlueprint: KnolusRuleBlueprint {
    val backing: ParserRuleContext

    override fun matchingText(): String = backing.text
    override fun toString(recognizer: Recognizer<*, *>): String = backing.toString(recognizer)
}
interface KnolusTypeBlueprint : KnolusRuleBlueprint

interface KnolusTokenBlueprint : KnolusBlueprint {
    enum class TokenType {
        IDENTIFIER,
        VARIABLE_DECLARATION,
        SET_VARIABLE,
        DECLARE_FUNCTION,
        END_SCOPE,
        SKIP_WS,
        SEMICOLON_SEPARATOR,
        NL_SEPARATOR,
        INTEGER,
        DECIMAL_NUMBER,
        TRUE,
        FALSE,
        NULL,
        GLOBAL,
        MODES_SKIP_WS,
        BEGIN_QUOTED_STRING,
        BEGIN_QUOTED_CHARACTER,
        BEGIN_EXPRESSION,
        BEGIN_FUNCTION_CALL,
        BEGIN_MEMBER_REFERENCE,
        BEGIN_ARRAY,
        START_FUNCTION_DECLARATION,
        END_FUNCTION_DECLARATION,
        END_DECL_WITH_STUB,
        END_DECL_WITH_BODY,
        FN_DECL_PARAM_SEPARATOR,
        FN_DECL_SKIP_WS,
        ARRAY_SEPARATOR,
        ARRAY_SKIP_WS,
        END_ARRAY,
        FUNC_CALL_SET_PARAMETER,
        FUNC_CALL_SEPARATOR,
        FUNC_CALL_SKIP_WS,
        END_FUNCTION_CALL,
        EXPRESSION_SKIP_WS,
        END_EXPRESSION,
        EXPR_EXPONENTIAL,
        EXPR_PLUS,
        EXPR_MINUS,
        EXPR_DIVIDE,
        EXPR_MULTIPLY,
        ESCAPES,
        STRING_CHARACTERS,
        QUOTED_STRING_VARIABLE_REFERENCE,
        QUOTED_STRING_LINE_BREAK,
        STRING_WHITESPACE,
        END_QUOTED_STRING,
        CHARACTER_ESCAPES,
        QUOTED_CHARACTERS,
        QUOTED_CHARACTER_LINE_BREAK,
        END_QUOTED_CHARACTER,
        PLAIN_CHARACTERS
    }

    val text: String
    val line: Int
    val charPositionInLine: Int
    val startIndex: Int?
    val stopIndex: Int?

    fun getTokenType(blueprint: ParserBlueprint<ParserRuleContext>): TokenType?

//    val channel:
}

inline fun ParserRuleContext.getToken(blueprint: ParserBlueprint<ParserRuleContext>, tokenType: KnolusTokenBlueprint.TokenType, i: Int): KnolusTokenBlueprint =
    TransTokenBlueprint(getToken(blueprint.tokenTypeToRaw.getValue(tokenType), i).symbol)

inline operator fun ParserRuleContext.get(blueprint: ParserBlueprint<ParserRuleContext>, tokenType: KnolusTokenBlueprint.TokenType, i: Int): KnolusTokenBlueprint? =
    blueprint.tokenTypeToRaw[tokenType]?.let { getToken(it, i)?.symbol }?.let(::TransTokenBlueprint)

inline fun ParserRuleContext.getToken(blueprint: ParserBlueprint<ParserRuleContext>, tokenType: KnolusTokenBlueprint.TokenType): KnolusTokenBlueprint = getToken(blueprint, tokenType, 0)
inline operator fun ParserRuleContext.get(blueprint: ParserBlueprint<ParserRuleContext>, tokenType: KnolusTokenBlueprint.TokenType): KnolusTokenBlueprint? = get(blueprint, tokenType, 0)

inline operator fun ParserRuleContext.get(klass: Class<out ParserRuleContext>, i: Int): ParserRuleContext? = getRuleContext(klass, i)
inline operator fun ParserRuleContext.get(klass: Class<out ParserRuleContext>): ParserRuleContext? = getRuleContext(klass, 0)

inline class TransTokenBlueprint(val backing: Token) : KnolusTokenBlueprint {
    override val text: String
        get() = backing.text
    override val line: Int
        get() = backing.line
    override val charPositionInLine: Int
        get() = backing.charPositionInLine
    override val startIndex: Int?
        get() = backing.startIndex.takeUnless { it == -1 }
    override val stopIndex: Int?
        get() = backing.stopIndex.takeUnless { it == -1 }

    override fun getTokenType(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint.TokenType? = blueprint.rawToTokenType[backing.type]
}

interface ScopeBlueprint : KnolusRuleBlueprint {
    fun getLines(blueprint: ParserBlueprint<ParserRuleContext>): List<LineBlueprint>
    fun getLine(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): LineBlueprint?
//    fun getSemicolonSeparatorTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<TerminalNode>
//    fun getSemicolonSeparatorToken(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): TerminalNode?
//    fun getNewLineSeparatorTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<TerminalNode>
//    fun getNewLineSeparatorToken(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): TerminalNode?
}

inline class TransScopeBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, ScopeBlueprint {
    override fun getLines(blueprint: ParserBlueprint<ParserRuleContext>): List<LineBlueprint> = backing.getRuleContexts(blueprint.lineContext).map(::TransLineBlueprint)
    override fun getLine(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): LineBlueprint? = backing.getRuleContext(blueprint.lineContext, index)?.let(::TransLineBlueprint)

//    override fun getSemicolonSeparatorTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<TerminalNode> = backing.getTokens(blueprint.semicolonSeparatorToken)
//    override fun getSemicolonSeparatorToken(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): TerminalNode? = backing.getToken(blueprint.semicolonSeparatorToken, index)
//
//    override fun getNewLineSeparatorTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<TerminalNode> = backing.getTokens(blueprint.newLineSeparatorToken)
//    override fun getNewLineSeparatorToken(blueprint: ParserBlueprint<ParserRuleContext>, index: Int): TerminalNode? = backing.getToken(blueprint.newLineSeparatorToken, index)
}

interface LineBlueprint : KnolusRuleBlueprint {
    fun getVariableDeclaration(blueprint: ParserBlueprint<ParserRuleContext>): DeclareVariableBlueprint?
    fun getFunctionDeclaration(blueprint: ParserBlueprint<ParserRuleContext>): DeclareFunctionBlueprint?
    fun getVariableAssignment(blueprint: ParserBlueprint<ParserRuleContext>): AssignVariableBlueprint?
    fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint?
    fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint?
}

inline class TransLineBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, LineBlueprint {
    override fun getVariableDeclaration(blueprint: ParserBlueprint<ParserRuleContext>): DeclareVariableBlueprint? = backing[blueprint.declareVariableContext]?.let(::TransDeclareVariableBlueprint)
    override fun getFunctionDeclaration(blueprint: ParserBlueprint<ParserRuleContext>): DeclareFunctionBlueprint? = backing[blueprint.declareFunctionContext]?.let(::TransDeclareFunctionBlueprint)
    override fun getVariableAssignment(blueprint: ParserBlueprint<ParserRuleContext>): AssignVariableBlueprint? = backing[blueprint.setVariableValueContext]?.let(::TransAssignVariableBlueprint)
    override fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint? = backing[blueprint.functionCallContext]?.let(::TransFunctionCallBlueprint)
    override fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint? = backing[blueprint.memberFunctionCallContext]?.let(::TransMemberFunctionCallBlueprint)
}

interface DeclareVariableBlueprint : KnolusRuleBlueprint {
    fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token

    fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean
    fun getVariableValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint?

//    fun getVariableDeclarationToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode
//    fun getGlobalToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode?

//    fun getVariableSetToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode?

}

inline class TransDeclareVariableBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, DeclareVariableBlueprint {
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token = blueprint.getVariableNameFromDeclareVariableContext(backing)

    override fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean = backing[blueprint, KnolusTokenBlueprint.TokenType.GLOBAL] != null
    override fun getVariableValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint? = backing[blueprint.variableValueContext]?.let(::TransVariableValueBlueprint)
}

interface AssignVariableBlueprint : KnolusRuleBlueprint {
    fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token

    //    fun getSetVariableToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode
    fun getVariableValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint
    fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean
//    fun getGlobalToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode?
}

inline class TransAssignVariableBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, AssignVariableBlueprint {
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token = blueprint.getVariableNameFromAssignVariableContext(backing)

    //    override fun getSetVariableToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode = backing.getToken(blueprint.setVariableToken, 0)
    override fun getVariableValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint = TransVariableValueBlueprint(backing.getRuleContext(blueprint.variableValueContext, 0))
    override fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean = backing[blueprint, KnolusTokenBlueprint.TokenType.GLOBAL] != null
}

interface DeclareFunctionBlueprint : KnolusRuleBlueprint {
    fun getFunctionNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token
    fun getParameterTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<Token>

    //    fun getDeclareFunctionToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode
//    fun getFunctionDeclarationStartToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode
//    fun getFunctionDeclarationEndToken(blueprint: ParserBlueprint<ParserRuleContext>): TerminalNode
    fun getFunctionDeclarationEndWithStubToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint?
    fun getFunctionDeclarationBody(blueprint: ParserBlueprint<ParserRuleContext>): DeclareFunctionBodyBlueprint?
    fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean
}

inline class TransDeclareFunctionBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, DeclareFunctionBlueprint {
    override fun getFunctionNameToken(blueprint: ParserBlueprint<ParserRuleContext>): Token = blueprint.getFunctionNameFromDeclareFunctionContext(backing)
    override fun getParameterTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<Token> = blueprint.getParametersFromDeclareFunctionContext(backing)

    override fun getFunctionDeclarationEndWithStubToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint? = backing[blueprint, KnolusTokenBlueprint.TokenType.END_DECL_WITH_STUB]

    override fun getFunctionDeclarationBody(blueprint: ParserBlueprint<ParserRuleContext>): DeclareFunctionBodyBlueprint? = backing[blueprint.declareFunctionBodyContext]?.let(::TransDeclareFunctionBodyBlueprint)

    override fun isGlobal(blueprint: ParserBlueprint<ParserRuleContext>): Boolean = backing[blueprint, KnolusTokenBlueprint.TokenType.GLOBAL] != null
}

interface DeclareFunctionBodyBlueprint : KnolusRuleBlueprint {
    fun getScope(blueprint: ParserBlueprint<ParserRuleContext>): ScopeBlueprint
}

inline class TransDeclareFunctionBodyBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, DeclareFunctionBodyBlueprint {
    override fun getScope(blueprint: ParserBlueprint<ParserRuleContext>): ScopeBlueprint = TransScopeBlueprint(backing.getRuleContext(blueprint.scopeContext, 0))
}

interface FunctionCallBlueprint : KnolusTypeBlueprint {
    fun getFunctionNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
    fun getFunctionParameters(blueprint: ParserBlueprint<ParserRuleContext>): List<FunctionCallParameterBlueprint>
}

inline class TransFunctionCallBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, FunctionCallBlueprint {
    override fun getFunctionNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = TransTokenBlueprint(blueprint.getFunctionNameFromFunctionCallContext(backing))
    override fun getFunctionParameters(blueprint: ParserBlueprint<ParserRuleContext>): List<FunctionCallParameterBlueprint> = blueprint.getFunctionParametersFromFunctionCallContext(backing).map(::TransFunctionCallParameterBlueprint)
}

interface FunctionCallParameterBlueprint : KnolusRuleBlueprint {
    fun getParameterNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint?
    fun getParameterValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint
}

inline class TransFunctionCallParameterBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, FunctionCallParameterBlueprint {
    override fun getParameterNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint? = blueprint.getParameterNameFromFunctionCallParameterContext(backing)?.let(::TransTokenBlueprint)
    override fun getParameterValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint = TransVariableValueBlueprint(backing.getRuleContext(blueprint.variableValueContext, 0))
}

interface MemberFunctionCallBlueprint : KnolusTypeBlueprint {
    fun getMemberNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
    fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint
}

inline class TransMemberFunctionCallBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, MemberFunctionCallBlueprint {
    override fun getMemberNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = TransTokenBlueprint(blueprint.getMemberNameFromMemberFunctionCallContext(backing))
    override fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint = TransFunctionCallBlueprint(backing.getRuleContext(blueprint.functionCallContext, 0))
}

interface VariableReferenceBlueprint : KnolusTypeBlueprint {
    fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
}

inline class TransVariableReferenceBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, VariableReferenceBlueprint {
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = TransTokenBlueprint(blueprint.getVariableNameFromVariableReferenceContext(backing))
}

interface MemberVariableReferenceBlueprint : VariableReferenceBlueprint {
    fun getMemberNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
}

inline class TransMemberVariableReferenceBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, MemberVariableReferenceBlueprint {
    override fun getMemberNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = TransTokenBlueprint(blueprint.getMemberNameFromMemberVariableReferenceContext(backing))
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint =
        TransTokenBlueprint(blueprint.getVariableNameFromVariableReferenceContext(backing.getRuleContext(blueprint.variableReferenceContext, 0)))
}

interface VariableValueBlueprint : KnolusRuleBlueprint {
    fun getArray(blueprint: ParserBlueprint<ParserRuleContext>): ArrayBlueprint?
    fun getQuotedString(blueprint: ParserBlueprint<ParserRuleContext>): QuotedStringBlueprint?
    fun getPlainString(blueprint: ParserBlueprint<ParserRuleContext>): PlainStringBlueprint?
    fun getQuotedCharacter(blueprint: ParserBlueprint<ParserRuleContext>): QuotedCharacterBlueprint?
    fun getNumber(blueprint: ParserBlueprint<ParserRuleContext>): NumberBlueprint?
    fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint?
    fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint?
    fun getExpression(blueprint: ParserBlueprint<ParserRuleContext>): ExpressionBlueprint?
    fun getBoolean(blueprint: ParserBlueprint<ParserRuleContext>): BooleanBlueprint?
    fun getNullToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint?
    fun getMemberVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): MemberVariableReferenceBlueprint?
    fun getVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): VariableReferenceBlueprint?
}

inline class TransVariableValueBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, VariableValueBlueprint {
    override fun getArray(blueprint: ParserBlueprint<ParserRuleContext>): ArrayBlueprint? = backing[blueprint.arrayContext]?.let(::TransArrayBlueprint)
    override fun getQuotedString(blueprint: ParserBlueprint<ParserRuleContext>): QuotedStringBlueprint? = backing[blueprint.quotedStringContext]?.let(::TransQuotedStringBlueprint)
    override fun getPlainString(blueprint: ParserBlueprint<ParserRuleContext>): PlainStringBlueprint? = backing[blueprint.plainStringContext]?.let(::TransPlainStringBlueprint)
    override fun getQuotedCharacter(blueprint: ParserBlueprint<ParserRuleContext>): QuotedCharacterBlueprint? = backing[blueprint.quotedCharacterContext]?.let(::TransQuotedCharacterBlueprint)
    override fun getNumber(blueprint: ParserBlueprint<ParserRuleContext>): NumberBlueprint? = backing[blueprint.numberContext]?.let(::TransNumberBlueprint)
    override fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint? = backing[blueprint.memberFunctionCallContext]?.let(::TransMemberFunctionCallBlueprint)
    override fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint? = backing[blueprint.functionCallContext]?.let(::TransFunctionCallBlueprint)
    override fun getExpression(blueprint: ParserBlueprint<ParserRuleContext>): ExpressionBlueprint? = backing[blueprint.expressionContext]?.let(::TransExpressionBlueprint)
    override fun getBoolean(blueprint: ParserBlueprint<ParserRuleContext>): BooleanBlueprint? = backing[blueprint.boolContext]?.let(::TransBooleanBlueprint)
    override fun getNullToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint? = backing[blueprint, KnolusTokenBlueprint.TokenType.NULL]
    override fun getMemberVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): MemberVariableReferenceBlueprint? =
        backing[blueprint.memberVariableReferenceContext]?.let(::TransMemberVariableReferenceBlueprint)

    override fun getVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): VariableReferenceBlueprint? = backing[blueprint.variableReferenceContext]?.let(::TransVariableReferenceBlueprint)
}

interface StringValueBlueprint : KnolusRuleBlueprint {
    fun getQuotedString(blueprint: ParserBlueprint<ParserRuleContext>): QuotedStringBlueprint?
    fun getPlainString(blueprint: ParserBlueprint<ParserRuleContext>): PlainStringBlueprint?
    fun getQuotedCharacter(blueprint: ParserBlueprint<ParserRuleContext>): QuotedCharacterBlueprint?
    fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint?
    fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint?
    fun getMemberVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): MemberVariableReferenceBlueprint?
    fun getVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): VariableReferenceBlueprint?
}

inline class TransStringValueBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, StringValueBlueprint {
    override fun getQuotedString(blueprint: ParserBlueprint<ParserRuleContext>): QuotedStringBlueprint? = backing[blueprint.quotedStringContext]?.let(::TransQuotedStringBlueprint)
    override fun getPlainString(blueprint: ParserBlueprint<ParserRuleContext>): PlainStringBlueprint? = backing[blueprint.plainStringContext]?.let(::TransPlainStringBlueprint)
    override fun getQuotedCharacter(blueprint: ParserBlueprint<ParserRuleContext>): QuotedCharacterBlueprint? = backing[blueprint.quotedCharacterContext]?.let(::TransQuotedCharacterBlueprint)
    override fun getMemberFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): MemberFunctionCallBlueprint? = backing[blueprint.memberFunctionCallContext]?.let(::TransMemberFunctionCallBlueprint)
    override fun getFunctionCall(blueprint: ParserBlueprint<ParserRuleContext>): FunctionCallBlueprint? = backing[blueprint.functionCallContext]?.let(::TransFunctionCallBlueprint)
    override fun getMemberVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): MemberVariableReferenceBlueprint? =
        backing[blueprint.memberVariableReferenceContext]?.let(::TransMemberVariableReferenceBlueprint)
    override fun getVariableReference(blueprint: ParserBlueprint<ParserRuleContext>): VariableReferenceBlueprint? = backing[blueprint.variableReferenceContext]?.let(::TransVariableReferenceBlueprint)
}

interface ArrayBlueprint : KnolusTypeBlueprint {
    fun getArrayContents(blueprint: ParserBlueprint<ParserRuleContext>): ArrayContentsBlueprint
}

inline class TransArrayBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, ArrayBlueprint {
    override fun getArrayContents(blueprint: ParserBlueprint<ParserRuleContext>): ArrayContentsBlueprint = TransArrayContentsBlueprint(backing.getRuleContext(blueprint.arrayContentsContext, 0))
}

interface ArrayContentsBlueprint : KnolusRuleBlueprint {
    fun getArrayElements(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusTypeBlueprint>
}

inline class TransArrayContentsBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, ArrayContentsBlueprint {
    override fun getArrayElements(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusTypeBlueprint> = backing.children.mapNotNull { ctx ->
        when {
            blueprint.quotedStringContext.isInstance(ctx) -> TransQuotedStringBlueprint(ctx as ParserRuleContext)
            blueprint.memberFunctionCallContext.isInstance(ctx) -> TransMemberFunctionCallBlueprint(ctx as ParserRuleContext)
            blueprint.functionCallContext.isInstance(ctx) -> TransFunctionCallBlueprint(ctx as ParserRuleContext)
            blueprint.expressionContext.isInstance(ctx) -> TransExpressionBlueprint(ctx as ParserRuleContext)
            blueprint.memberVariableReferenceContext.isInstance(ctx) -> TransMemberVariableReferenceBlueprint(ctx as ParserRuleContext)
            blueprint.variableReferenceContext.isInstance(ctx) -> TransVariableReferenceBlueprint(ctx as ParserRuleContext)
            blueprint.quotedCharacterContext.isInstance(ctx) -> TransQuotedCharacterBlueprint(ctx as ParserRuleContext)
            blueprint.wholeNumberContext.isInstance(ctx) -> TransWholeNumberBlueprint(ctx as ParserRuleContext)
            blueprint.decimalNumberContext.isInstance(ctx) -> TransDecimalNumberBlueprint(ctx as ParserRuleContext)
            blueprint.boolContext.isInstance(ctx) -> TransBooleanBlueprint(ctx as ParserRuleContext)
            else -> null
        }
    }
}

interface BooleanBlueprint : KnolusTypeBlueprint {
    fun parseBoolean(blueprint: ParserBlueprint<ParserRuleContext>): Boolean
}

inline class TransBooleanBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, BooleanBlueprint {
    override fun parseBoolean(blueprint: ParserBlueprint<ParserRuleContext>): Boolean = backing[blueprint, KnolusTokenBlueprint.TokenType.TRUE] != null
}

interface QuotedStringBlueprint : KnolusTypeBlueprint {
    fun getComponents(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusBlueprint>
}

inline class TransQuotedStringBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, QuotedStringBlueprint {
    override fun getComponents(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusBlueprint> = backing.children.mapNotNull { ctx ->
        if (blueprint.quotedStringVariableReferenceContext.isInstance(ctx)) {
            return@mapNotNull TransQuotedStringVariableReferenceBlueprint(ctx as ParserRuleContext)
        } else if (ctx !is TerminalNode) {
            return@mapNotNull null
        }

        return@mapNotNull when (blueprint.rawToTokenType[ctx.symbol.type]) {
            KnolusTokenBlueprint.TokenType.ESCAPES -> TransTokenBlueprint(ctx.symbol)
            KnolusTokenBlueprint.TokenType.STRING_CHARACTERS -> TransTokenBlueprint(ctx.symbol)
            KnolusTokenBlueprint.TokenType.QUOTED_STRING_LINE_BREAK -> TransTokenBlueprint(ctx.symbol)
            KnolusTokenBlueprint.TokenType.STRING_WHITESPACE -> TransTokenBlueprint(ctx.symbol)

            else -> null
        }
    }
}

interface PlainStringBlueprint : KnolusTypeBlueprint {
    fun getTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusTokenBlueprint>
}

inline class TransPlainStringBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, PlainStringBlueprint {
    override fun getTokens(blueprint: ParserBlueprint<ParserRuleContext>): List<KnolusTokenBlueprint> = backing.children.mapNotNull { ctx ->
        if (ctx !is TerminalNode) {
            return@mapNotNull null
        }

        return@mapNotNull when (blueprint.rawToTokenType[ctx.symbol.type]) {
            KnolusTokenBlueprint.TokenType.ESCAPES -> TransTokenBlueprint(ctx.symbol)
            KnolusTokenBlueprint.TokenType.PLAIN_CHARACTERS -> TransTokenBlueprint(ctx.symbol)

            else -> null
        }
    }
}

interface QuotedCharacterBlueprint : KnolusTypeBlueprint {
    fun getCharacterToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
}

inline class TransQuotedCharacterBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, QuotedCharacterBlueprint {
    override fun getCharacterToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint =
        requireNotNull(
            backing[blueprint, KnolusTokenBlueprint.TokenType.CHARACTER_ESCAPES]
            ?: backing[blueprint, KnolusTokenBlueprint.TokenType.QUOTED_CHARACTERS]
            ?: backing[blueprint, KnolusTokenBlueprint.TokenType.QUOTED_CHARACTER_LINE_BREAK]
        )
}

interface QuotedStringVariableReferenceBlueprint : VariableReferenceBlueprint

inline class TransQuotedStringVariableReferenceBlueprint(override val backing: ParserRuleContext) : TransRuleBlueprint, QuotedStringVariableReferenceBlueprint {
    override fun getVariableNameToken(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint =
        TransTokenBlueprint(blueprint.getVariableNameFromVariableReferenceContext(backing.getRuleContext(blueprint.variableReferenceContext, 0)))
}

interface NumberBlueprint: KnolusTypeBlueprint {
    fun getWholeNumber(blueprint: ParserBlueprint<ParserRuleContext>): WholeNumberBlueprint?
    fun getDecimalNumber(blueprint: ParserBlueprint<ParserRuleContext>): DecimalNumberBlueprint?
}

inline class TransNumberBlueprint(override val backing: ParserRuleContext): TransRuleBlueprint, NumberBlueprint {
    override fun getWholeNumber(blueprint: ParserBlueprint<ParserRuleContext>): WholeNumberBlueprint? = backing[blueprint.wholeNumberContext]?.let(::TransWholeNumberBlueprint)
    override fun getDecimalNumber(blueprint: ParserBlueprint<ParserRuleContext>): DecimalNumberBlueprint? = backing[blueprint.decimalNumberContext]?.let(::TransDecimalNumberBlueprint)
}

interface WholeNumberBlueprint: KnolusTypeBlueprint {
    fun getInteger(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
}

inline class TransWholeNumberBlueprint(override val backing: ParserRuleContext): TransRuleBlueprint, WholeNumberBlueprint {
    override fun getInteger(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = backing.getToken(blueprint, KnolusTokenBlueprint.TokenType.INTEGER)
}

interface DecimalNumberBlueprint: KnolusTypeBlueprint {
    fun getDecimalNumber(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint
}

inline class TransDecimalNumberBlueprint(override val backing: ParserRuleContext): TransRuleBlueprint, DecimalNumberBlueprint {
    override fun getDecimalNumber(blueprint: ParserBlueprint<ParserRuleContext>): KnolusTokenBlueprint = backing.getToken(blueprint, KnolusTokenBlueprint.TokenType.DECIMAL_NUMBER)
}

interface ExpressionBlueprint: KnolusTypeBlueprint {
    fun getStartingValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint
    fun getOperations(blueprint: ParserBlueprint<ParserRuleContext>): List<ExpressionOperator>
    fun getValues(blueprint: ParserBlueprint<ParserRuleContext>): List<VariableValueBlueprint>

    fun getZippedExpression(blueprint: ParserBlueprint<ParserRuleContext>): List<Pair<ExpressionOperator, VariableValueBlueprint>>
}

inline class TransExpressionBlueprint(override val backing: ParserRuleContext): TransRuleBlueprint, ExpressionBlueprint {
    override fun getStartingValue(blueprint: ParserBlueprint<ParserRuleContext>): VariableValueBlueprint = TransVariableValueBlueprint(blueprint.getStartingValueFromExpressionContext(backing))
    override fun getOperations(blueprint: ParserBlueprint<ParserRuleContext>): List<ExpressionOperator> = blueprint.getExpressionOperationsFromExpressionContext(backing).map(blueprint::contextToOperator)
    override fun getValues(blueprint: ParserBlueprint<ParserRuleContext>): List<VariableValueBlueprint> = blueprint.getExpressionValuesFromExpressionContext(backing).map(::TransVariableValueBlueprint)

    override fun getZippedExpression(blueprint: ParserBlueprint<ParserRuleContext>): List<Pair<ExpressionOperator, VariableValueBlueprint>> = getOperations(blueprint).zip(getValues(blueprint))
}


