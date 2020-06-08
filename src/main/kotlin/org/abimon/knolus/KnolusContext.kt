package org.abimon.knolus

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

        return a.typeInfo.getMemberFunctionNames(operator.functionCallName)
            .fold(null as KnolusTypedValue?) { acc, funcName ->
                acc ?: invokeFunction(funcName, params)
            }
    }

    suspend fun invokeFunction(
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusTypedValue? {
//        val flattened = functionParameters.map { (name, value) ->
//            if (name != null) KnolusUnion.FunctionParameterType(
//                name,
//                value.evaluateOrSelf(this)
//            ) else KnolusUnion.FunctionParameterType(null, value.evaluateOrSelf(this))
//        }

//        println("Calling $functionName(${flattened.map { value -> "${value.name}=${value.parameter.asString(this)}" }.joinToString()})")

        val function = functionRegistry[functionName.sanitiseFunctionIdentifier()]
            ?.firstOrNull { func -> (functionParameters.size >= func.parameters.count { (_, default) -> default == null } && functionParameters.size <= func.parameters.size) || (func.variadicSupported && functionParameters.size >= func.parameters.size) }
            ?: return parent?.invokeFunction(functionName, functionParameters)

        val functionParams = function.parameters.toMutableList()
        val passedParams: MutableMap<String, KnolusTypedValue> = HashMap()
        functionParameters.filter { union ->
            //            if (union !is PipelineUnion.FunctionParameterType) return@forEach
            val parameter = functionParams.firstOrNull { (p) ->
                p == union.name?.sanitiseFunctionIdentifier()
            } ?: return@filter true

            passedParams[parameter.first] = union.parameter
            functionParams.remove(parameter)

            false
        }.forEach { union ->
            passedParams[union.name?.sanitiseFunctionIdentifier()
                ?: if (functionParams.isNotEmpty()) functionParams.removeAt(0).first else "INDEX${passedParams.size}"] =
                union.parameter
        }

        functionParams.forEach { (name, default) -> passedParams.putIfAbsent(name, default ?: return@forEach) }

        return function.suspendInvoke(this, passedParams) ?: KnolusConstants.Null
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

private inline fun functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?>()

/** Regular Spec Functions */

fun KnolusContext.registerFunction(
    functionName: String,
    func: suspend (context: KnolusContext) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(func)
        .build()
)

fun KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(func)
        .build()
)

fun <P> KnolusContext.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<P>,
    func: suspend (context: KnolusContext, parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<P>,
    func: suspend (context: KnolusContext, parameter: P) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(parameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    thirdParameterSpec: ParameterSpec<P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <P1, P2, P3> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterName: ParameterSpec<P1>,
    secondParameterName: ParameterSpec<P2>,
    thirdParameterName: ParameterSpec<P3>,
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterName, secondParameterName, thirdParameterName, func)
        .build()
)

/** Register member functions */

fun <P0> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, func)
        .build()
)

fun <P0> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    parameterSpec: ParameterSpec<P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    parameterSpec: ParameterSpec<P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder()
        .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

/** Getter */

fun <P0> KnolusContext.registerMemberPropertyGetter(
    typeSpec: TypeParameterSpec<P0>,
    propertyName: String,
    func: suspend (context: KnolusContext, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder()
        .setFunction(typeSpec, func)
        .build()
)