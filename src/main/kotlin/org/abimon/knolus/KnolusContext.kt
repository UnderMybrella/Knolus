package org.abimon.knolus

import org.abimon.antlr.knolus.KnolusLexer
import org.abimon.antlr.knolus.KnolusParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

fun toStringTree(t: Tree, recog: Parser?): String? {
    val ruleNames = recog?.ruleNames
    val ruleNamesList = if (ruleNames != null) listOf(*ruleNames) else null
    return toStringTree(t, ruleNamesList)
}

/** Print out a whole tree in LISP form. [.getNodeText] is used on the
 * node payloads to get the text for the nodes.
 */
fun toStringTree(t: Tree, ruleNames: List<String?>?, indent: Int = 0): String? {
    var s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    if (t.childCount == 0) return s
    val buf = StringBuilder()
//    buf.append("(")
    buf.appendln()
    repeat(indent) { buf.append('\t') }
    buf.append("> ")
    s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    buf.append(s)
    buf.append(' ')
    for (i in 0 until t.childCount) {
        if (i > 0) buf.append(' ')
        buf.append(toStringTree(t.getChild(i), ruleNames, indent + 1))
    }
//    buf.append(")")
    return buf.toString()
}

@Suppress("UnnecessaryVariable")
@ExperimentalUnsignedTypes
fun parseKnolusScope(text: String): KnolusResult<KnolusUnion.ScopeType> {
    val charStream = CharStreams.fromString(text)
    val lexer = KnolusLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = KnolusParser(tokens)
    val visitor = KnolusVisitor(parser)
    val union = visitor.visitScope(parser.scope())
    return union
}

@ExperimentalUnsignedTypes
class KnolusFunction<T>(val name: String, vararg val parameters: Pair<String, KnolusUnion.VariableValue?>, val variadicSupported: Boolean = false, val func: suspend (context: KnolusContext, parameters: Map<String, KnolusUnion.VariableValue>) -> T) {
    suspend fun suspendInvoke(context: KnolusContext, parameters: Map<String, KnolusUnion.VariableValue>) = func(context, parameters)
}

class KnolusFunctionBuilder<T>(val name: String) {
    val parameters: MutableList<Pair<String, KnolusUnion.VariableValue?>> = ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (context: KnolusContext, parameters: Map<String, KnolusUnion.VariableValue>) -> T

    fun addParameter(name: String, default: KnolusUnion.VariableValue? = null) {
        parameters.add(Pair(name.sanitiseFunctionIdentifier(), default))
    }

    fun addFlag(name: String, default: Boolean = false) = addParameter(name, KnolusUnion.VariableValue.BooleanType(default))

    fun setFunction(func: suspend (context: KnolusContext, parameters: Map<String, KnolusUnion.VariableValue>) -> T) {
        this.func = func
    }

    fun build() = KnolusFunction(name, *parameters.toTypedArray(), variadicSupported = variadicSupported, func = func)
}

@ExperimentalUnsignedTypes
class KnolusContext(val parent: KnolusContext?) {
    private val variableRegistry: MutableMap<String, KnolusUnion.VariableValue> = HashMap()
    private val functionRegistry: MutableMap<String, MutableList<KnolusFunction<KnolusUnion.VariableValue?>>> = HashMap()

    operator fun get(key: String): KnolusUnion.VariableValue? = variableRegistry[key] ?: parent?.get(key)

    operator fun set(key: String, global: Boolean = false, value: KnolusUnion.VariableValue) {
        if (global && parent != null) {
            parent[key, global] = value
        } else {
            variableRegistry[key] = value
        }
    }

    operator fun contains(key: String): Boolean = key in variableRegistry || (parent?.contains(key) ?: false)

    fun register(name: String, init: KnolusFunctionBuilder<KnolusUnion.VariableValue?>.() -> Unit) {
        val builder = KnolusFunctionBuilder<KnolusUnion.VariableValue?>(name)
        builder.init()
        register(name, builder.build())
    }

    fun register(name: String, func: suspend (context: KnolusContext, parameters: Map<String, KnolusUnion.VariableValue>) -> KnolusUnion.VariableValue?, init: KnolusFunctionBuilder<KnolusUnion.VariableValue?>.() -> Unit) {
        val builder = KnolusFunctionBuilder<KnolusUnion.VariableValue?>(name)
        builder.setFunction(func)
        builder.init()
        register(name, builder.build())
    }

    fun register(name: String, func: KnolusFunction<KnolusUnion.VariableValue?>, global: Boolean = false) {
        if (global && parent != null) {
            parent.register(name, func, global)
        } else {
            val functions: MutableList<KnolusFunction<KnolusUnion.VariableValue?>>
            if (name.sanitiseFunctionIdentifier() !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[name.sanitiseFunctionIdentifier()] = functions
            } else {
                functions = functionRegistry.getValue(name.sanitiseFunctionIdentifier())
            }

            functions.add(func)
        }
    }

    suspend fun invokeScript(scriptName: String, scriptParameters: Array<KnolusUnion.ScriptParameterType>): KnolusUnion.VariableValue? = invokeFunction(scriptName, Array(scriptParameters.size) { i -> KnolusUnion.FunctionParameterType(scriptParameters[i].name, scriptParameters[i].parameter) })
    suspend fun invokeFunction(functionName: String, functionParameters: Array<KnolusUnion.FunctionParameterType>): KnolusUnion.VariableValue? {
        val flattened = functionParameters.map { (name, value) ->
            if (name != null) KnolusUnion.FunctionParameterType(name, value) else KnolusUnion.FunctionParameterType(null, value.flatten(this))
        }

//        println("Calling $functionName(${flattened.map { value -> "${value.name}=${value.parameter.asString(this)}" }.joinToString()})")

        val flatPassed = flattened.count()
        val function = functionRegistry[functionName.sanitiseFunctionIdentifier()]
                ?.firstOrNull { func -> (flatPassed >= func.parameters.count { (_, default) -> default == null } && flatPassed <= func.parameters.size) || (func.variadicSupported && flatPassed >= func.parameters.size) }
                ?: return parent?.invokeFunction(functionName, flattened.toTypedArray())

        val functionParams = function.parameters.toMutableList()
        val passedParams: MutableMap<String, KnolusUnion.VariableValue> = HashMap()
        flattened.filter { union ->
            //            if (union !is PipelineUnion.FunctionParameterType) return@forEach
            val parameter = functionParams.firstOrNull { (p) ->
                p == union.name?.sanitiseFunctionIdentifier()
            } ?: return@filter true

            passedParams[parameter.first] = union.parameter
            functionParams.remove(parameter)

            false
        }.forEach { union ->
            passedParams[union.name?.sanitiseFunctionIdentifier()
                    ?: if (functionParams.isNotEmpty()) functionParams.removeAt(0).first else "INDEX${passedParams.size}"] = union.parameter
        }

        functionParams.forEach { (name, default) -> passedParams.putIfAbsent(name, default ?: return@forEach) }

        return function.suspendInvoke(this, passedParams)
    }

    init {
//        PipelineFunctions.registerAll(this)
    }
}

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.run(parentContext: KnolusContext? = null, parameters: Map<String, Any?> = emptyMap(), init: KnolusContext.() -> Unit = {}): KnolusUnion.VariableValue? {
    val knolusContext = KnolusContext(parentContext)
    parameters.forEach { (k, v) -> knolusContext[k] = v as? KnolusUnion.VariableValue ?: return@forEach }

    knolusContext.init()

    lines.forEach { union ->
        when (union) {
            is KnolusUnion.Action -> union.run(knolusContext)
            is KnolusUnion.ReturnStatement -> return union.value.flatten(knolusContext)
        }
    }

    return null
}

private val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
private fun String.sanitiseFunctionIdentifier(): String = toUpperCase().replace(SEPARATOR_CHARACTERS, "")