package dev.brella.knolus.transform

import org.abimon.kornea.errors.common.KorneaResult
import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeVisitor

abstract class KnolusParserState<R, L : Lexer, T : BufferedTokenStream, P : Parser, V : ParseTreeVisitor<KorneaResult<R>>> {
    abstract val lexer: L
    abstract val tokenStream: T
    abstract val parser: P
    abstract val visitor: V
}

data class KnolusParserResult<R, L : Lexer, T : BufferedTokenStream, P : Parser, V : ParseTreeVisitor<KorneaResult<R>>>(
    val result: R,
    override val lexer: L,
    override val tokenStream: T,
    override val parser: P,
    override val visitor: V
) : KnolusParserState<R, L, T, P, V>()