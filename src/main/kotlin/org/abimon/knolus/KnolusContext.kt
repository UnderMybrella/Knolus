package org.abimon.knolus

import org.abimon.knolus.modules.functionregistry.functionBuilder
import org.abimon.knolus.modules.functionregistry.setFunction
import org.abimon.knolus.types.*
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

@ExperimentalUnsignedTypes
class KnolusContext(val parent: KnolusContext?) {
    private val variableRegistry: MutableMap<String, KnolusTypedValue> = HashMap()
    private val functionRegistry: MutableMap<String, MutableList<KnolusFunction<KnolusTypedValue?>>> =
        HashMap()

    operator fun get(key: String): KnolusTypedValue? = variableRegistry[key] ?: parent?.get(key)

    operator fun set(key: String, global: Boolean = false, value: KnolusTypedValue) {
        if (global && parent != null) {
            parent[key, global] = value
        } else {
            variableRegistry[key] = value
        }
    }

    operator fun contains(key: String): Boolean = key in variableRegistry || (parent?.contains(key) ?: false)

    fun register(name: String, init: KnolusFunctionBuilder<KnolusTypedValue?>.() -> Unit) {
        val builder = KnolusFunctionBuilder<KnolusTypedValue?>()
        builder.init()
        register(name, builder.build())
    }

    fun register(
        name: String,
        func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> KnolusTypedValue?,
        init: KnolusFunctionBuilder<KnolusTypedValue?>.() -> Unit,
    ) {
        val builder = KnolusFunctionBuilder<KnolusTypedValue?>()
        builder.setFunction(func)
        builder.init()
        register(name, builder.build())
    }

    fun register(name: String, func: KnolusFunction<KnolusTypedValue?>, global: Boolean = false) {
        if (global && parent != null) {
            parent.register(name, func, global)
        } else {
            val functions: MutableList<KnolusFunction<KnolusTypedValue?>>
            if (name.sanitiseFunctionIdentifier() !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[name.sanitiseFunctionIdentifier()] = functions
            } else {
                functions = functionRegistry.getValue(name.sanitiseFunctionIdentifier())
            }

            functions.add(func)
        }
    }

//    suspend fun invokeScript(
//        scriptName: String,
//        scriptParameters: Array<KnolusUnion.ScriptParameterType>
//    ): KnolusTypedValue? = invokeFunction(
//        scriptName,
//        Array(scriptParameters.size) { i ->
//            KnolusUnion.FunctionParameterType(
//                scriptParameters[i].name,
//                scriptParameters[i].parameter
//            )
//        })

    suspend fun invokeMemberFunction(
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusTypedValue? {
        val params = arrayOfNulls<KnolusUnion.FunctionParameterType>(functionParameters.size + 1)
        params[0] = KnolusUnion.FunctionParameterType("self", member)
        functionParameters.copyInto(params, 1)

        @Suppress("UNCHECKED_CAST")
        params as Array<KnolusUnion.FunctionParameterType>

        return member.typeInfo.getMemberFunctionNames(functionName).fold(null as KnolusTypedValue?) { acc, funcName ->
            acc ?: invokeFunction(funcName, params)
        }
    }

    suspend fun invokeMemberPropertyGetter(member: KnolusTypedValue, propertyName: String): KnolusTypedValue? {
        val params = arrayOf(KnolusUnion.FunctionParameterType("self", member))

        return member.typeInfo.getMemberPropertyGetterNames(propertyName)
            .fold(null as KnolusTypedValue?) { acc, funcName ->
                acc ?: invokeFunction(funcName, params)
            }
    }

    suspend fun invokeOperator(
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KnolusTypedValue? {
        val params = arrayOf(KnolusUnion.FunctionParameterType("a", a), KnolusUnion.FunctionParameterType("b", b))

        return a.typeInfo.getMemberOperatorNames(operator)
            .fold(null as KnolusTypedValue?) { acc, funcName ->
                acc ?: invokeFunction(funcName, params)
            }
    }

    suspend fun invokeFunction(
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusTypedValue? {
        functionRegistry[functionName.sanitiseFunctionIdentifier()]?.forEach functionList@{ function ->
            val unmappedParams = function.parameters.toMutableList()
            val passedParams: MutableMap<String, KnolusTypedValue> = HashMap()
            val usedAllParams = functionParameters.filter { union ->
                val parameter = unmappedParams.firstOrNull(union::matches) ?: return@filter true

                passedParams[parameter.name] = union.parameter
                unmappedParams.remove(parameter)

                false
            }.filter { union ->
                val parameter = unmappedParams.firstOrNull(union::fits) ?: return@functionList

                passedParams[parameter.name] = union.parameter
                unmappedParams.remove(parameter)

                false
            }.isEmpty()

            if (!usedAllParams) return@functionList

            unmappedParams.forEach { param ->
                when (param.missingPolicy) {
                    KnolusFunctionParameterMissingPolicy.Mandatory -> return@functionList
                    KnolusFunctionParameterMissingPolicy.Optional -> return@forEach
                    is KnolusFunctionParameterMissingPolicy.Substitute<*> ->
                        passedParams.putIfAbsent(param.name, param.missingPolicy.default)
                }
            }

            return function.suspendInvoke(this, passedParams) ?: KnolusConstants.Null
        }

        return parent?.invokeFunction(functionName, functionParameters) ?: KnolusConstants.Undefined
    }
}

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.run(
    parentContext: KnolusContext? = null,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {},
): KnolusTypedValue? {
    val knolusContext = KnolusContext(parentContext)
    parameters.forEach { (k, v) -> knolusContext[k] = v as? KnolusTypedValue ?: return@forEach }

    knolusContext.init()

    lines.forEach { union ->
        when (union) {
            is KnolusUnion.Action -> union.run(knolusContext)
            is KnolusUnion.VariableValue<*> ->
                if (union.value is KnolusUnion.Action)
                    (union.value as KnolusUnion.Action).run(knolusContext)
            is KnolusUnion.ReturnStatement -> return union.value.evaluateOrSelf(knolusContext)
        }
    }

    return null
}

internal val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
internal fun String.sanitiseFunctionIdentifier(): String = toUpperCase().replace(SEPARATOR_CHARACTERS, "")

/** Getter */

fun <P0> KnolusContext.registerMemberPropertyGetter(
    typeSpec: ParameterSpec<*, P0>,
    propertyName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder()
        .setFunction(typeSpec, func)
        .build()
)

/** Operator */

fun <P0, P1> KnolusContext.registerOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpec: ParameterSpec<*, P1>,
    func: suspend (context: KnolusContext, a: P0, b: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberOperatorName(operator),
    functionBuilder()
        .setOperatorFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMultiOperatorFunction(
    typeSpec: ParameterSpec<*, P0>,
    operator: ExpressionOperator,
    parameterSpecs: Array<ParameterSpec<*, P1>>,
    func: suspend (context: KnolusContext, a: P0, b: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder()
            .setOperatorFunction(typeSpec, parameterSpec, func)
            .build()
    )
}