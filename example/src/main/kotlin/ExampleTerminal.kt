import kotlinx.coroutines.runBlocking
import org.abimon.antlr.knolus.ExampleLexer
import org.abimon.antlr.knolus.ExampleParser
import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusGlobalContext
import dev.brella.knolus.modules.KnolusStringModule
import dev.brella.knolus.modules.functionregistry.*
import dev.brella.knolus.modules.functionregistry.registerFunction
import dev.brella.knolus.restrictions.CompoundKnolusRestriction
import dev.brella.knolus.restrictions.KnolusRecursiveRestriction
import dev.brella.knolus.transform.*
import dev.brella.knolus.types.KnolusInt
import dev.brella.knolus.types.KnolusString
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asNumber
import org.abimon.kornea.errors.common.*
import org.antlr.v4.runtime.CommonTokenStream

@ExperimentalUnsignedTypes
suspend fun main(args: Array<String>) {
    val context = KnolusGlobalContext(null, CompoundKnolusRestriction.fromPermissive(KnolusRecursiveRestriction(maxDepth = 20, maxRecursiveCount = 30)))
    with(context) {
        KnolusStringModule.register(this)

        registerFunctionWithoutReturn("println", objectTypeAsStringParameter("msg")) { msg -> println(msg) }

        registerFunctionWithContext("factorial", intTypeParameter("i")) { context, i ->
            println("Factorial of: $i")
            if (i <= 1) return@registerFunctionWithContext KnolusInt(i)
            val factorialResult = context.invokeFunction("factorial", KnolusInt(i - 1))
                .flatMap { it.asNumber(context) }
                .get()
                .toInt()

            return@registerFunctionWithContext KnolusInt(i * factorialResult)
        }

        registerUntypedFunction("version") { context -> KnolusString("Version 1.2.0") }
        registerFunctionWithoutReturn("echo", objectTypeAsStringParameter("line")) { line -> println(line) }
    }

    var state: KnolusParserState<KnolusUnion, ExampleLexer, CommonTokenStream, ExampleParser, ExampleVisitor>? = null

    suspend fun handle(value: Any): KorneaResult<Any?> =
        when (value) {
            is KnolusUnion.Action<*> -> value.run(context)
            is KnolusUnion.VariableValue<*> -> handle(value.value)
            is KnolusUnion.ReturnStatement -> KorneaResult.success(value.value)
            else -> KorneaResult.empty()
        }

    while (true) {
        print("> ")
        val line = readLine() ?: break
        val result: KnolusUnion

        if (state == null) {
            val parseResult = parseKnolusTransRule(line, KnolusTransVisitorRestrictions.Permissive, ::ExampleLexer, ::ExampleParser, ::ExampleVisitor) { parser, visitor ->
                visitor.visitCommand(parser.command())
            }.get()

            result = parseResult.result
            state = parseResult
        } else {
            result = parseKnolusTransRuleWithState(line, state) { parser, visitor -> visitor.visitCommand(parser.command()) }.get()
        }

        handle(result).doOnSuccess { println(it) }.doOnFailure { println("ERR: $it") }
    }
}