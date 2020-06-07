package org.abimon.knolus

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
    private val variableRegistry: MutableMap<String, VariableValue> = HashMap()
    private val functionRegistry: MutableMap<String, MutableList<KnolusFunction<VariableValue?>>> =
        HashMap()

    operator fun get(key: String): VariableValue? = variableRegistry[key] ?: parent?.get(key)

    operator fun set(key: String, global: Boolean = false, value: VariableValue) {
        if (global && parent != null) {
            parent[key, global] = value
        } else {
            variableRegistry[key] = value
        }
    }

    operator fun contains(key: String): Boolean = key in variableRegistry || (parent?.contains(key) ?: false)

    fun register(name: String, init: KnolusFunctionBuilder<VariableValue?>.() -> Unit) {
        val builder = KnolusFunctionBuilder<VariableValue?>()
        builder.init()
        register(name, builder.build())
    }

    fun register(
        name: String,
        func: suspend (context: KnolusContext, parameters: Map<String, VariableValue>) -> VariableValue?,
        init: KnolusFunctionBuilder<VariableValue?>.() -> Unit
    ) {
        val builder = KnolusFunctionBuilder<VariableValue?>()
        builder.setFunction(func)
        builder.init()
        register(name, builder.build())
    }

    fun register(name: String, func: KnolusFunction<VariableValue?>, global: Boolean = false) {
        if (global && parent != null) {
            parent.register(name, func, global)
        } else {
            val functions: MutableList<KnolusFunction<VariableValue?>>
            if (name.sanitiseFunctionIdentifier() !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[name.sanitiseFunctionIdentifier()] = functions
            } else {
                functions = functionRegistry.getValue(name.sanitiseFunctionIdentifier())
            }

            functions.add(func)
        }
    }

    suspend fun invokeScript(
        scriptName: String,
        scriptParameters: Array<KnolusUnion.ScriptParameterType>
    ): VariableValue? = invokeFunction(
        scriptName,
        Array(scriptParameters.size) { i ->
            KnolusUnion.FunctionParameterType(
                scriptParameters[i].name,
                scriptParameters[i].parameter
            )
        })

    suspend fun invokeFunction(
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>
    ): VariableValue? {
        val flattened = functionParameters.map { (name, value) ->
            if (name != null) KnolusUnion.FunctionParameterType(
                name,
                value
            ) else KnolusUnion.FunctionParameterType(null, value.flatten(this))
        }

//        println("Calling $functionName(${flattened.map { value -> "${value.name}=${value.parameter.asString(this)}" }.joinToString()})")

        val flatPassed = flattened.count()
        val function = functionRegistry[functionName.sanitiseFunctionIdentifier()]
            ?.firstOrNull { func -> (flatPassed >= func.parameters.count { (_, default) -> default == null } && flatPassed <= func.parameters.size) || (func.variadicSupported && flatPassed >= func.parameters.size) }
            ?: return parent?.invokeFunction(functionName, flattened.toTypedArray())

        val functionParams = function.parameters.toMutableList()
        val passedParams: MutableMap<String, VariableValue> = HashMap()
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
                ?: if (functionParams.isNotEmpty()) functionParams.removeAt(0).first else "INDEX${passedParams.size}"] =
                union.parameter
        }

        functionParams.forEach { (name, default) -> passedParams.putIfAbsent(name, default ?: return@forEach) }

        return function.suspendInvoke(this, passedParams) ?: VariableValue.NullType
    }
}

@ExperimentalUnsignedTypes
suspend fun KnolusUnion.ScopeType.run(
    parentContext: KnolusContext? = null,
    parameters: Map<String, Any?> = emptyMap(),
    init: KnolusContext.() -> Unit = {}
): VariableValue? {
    val knolusContext = KnolusContext(parentContext)
    parameters.forEach { (k, v) -> knolusContext[k] = v as? VariableValue ?: return@forEach }

    knolusContext.init()

    lines.forEach { union ->
        when (union) {
            is KnolusUnion.Action -> union.run(knolusContext)
            is KnolusUnion.ReturnStatement -> return union.value.flatten(knolusContext)
        }
    }

    return null
}

internal val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
internal fun String.sanitiseFunctionIdentifier(): String = toUpperCase().replace(SEPARATOR_CHARACTERS, "")

private inline fun functionBuilder() = KnolusFunctionBuilder<VariableValue?>()

/** Regular function registers */
fun KnolusContext.registerFunction(
    functionName: String,
    parameterName: String,
    func: suspend (context: KnolusContext, parameter: VariableValue) -> VariableValue
) = register(
    functionName,
    functionBuilder()
        .setFunction(parameterName, func)
        .build()
)

fun KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    parameterName: String,
    func: suspend (context: KnolusContext, parameter: VariableValue) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(parameterName, func)
        .build()
)

fun KnolusContext.registerFunction(
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue) -> VariableValue
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterName, secondParameterName, func)
        .build()
)

fun KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterName, secondParameterName, func)
        .build()
)

fun KnolusContext.registerFunction(
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    thirdParameterName: String,
    func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue, thirdParameter: VariableValue) -> VariableValue
) = register(
    functionName,
    functionBuilder()
        .setFunction(firstParameterName, secondParameterName, thirdParameterName, func)
        .build()
)

fun KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    thirdParameterName: String,
    func: suspend (context: KnolusContext, firstParameter: VariableValue, secondParameter: VariableValue, thirdParameter: VariableValue) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterName, secondParameterName, thirdParameterName, func)
        .build()
)

/** Regular Spec Functions */

fun <P> KnolusContext.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<P>,
    func: suspend (context: KnolusContext, parameter: P) -> VariableValue
) = register(
    functionName,
    functionBuilder()
        .setFunction(parameterSpec, func)
        .build()
)

fun <P> KnolusContext.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<P>,
    func: suspend (context: KnolusContext, parameter: P) -> Unit
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> VariableValue
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> VariableValue
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
    func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit
) = register(
    functionName,
    functionBuilder()
        .setFunctionWithoutReturn(firstParameterName, secondParameterName, thirdParameterName, func)
        .build()
)

/** Register member functions */

fun KnolusContext.registerMemberFunction(
    typeName: String,
    functionName: String,
    func: suspend (context: KnolusContext, self: VariableValue) -> VariableValue
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction("self", func)
        .build()
)

fun KnolusContext.registerMemberFunctionWithoutReturn(
    typeName: String,
    functionName: String,
    func: suspend (context: KnolusContext, self: VariableValue) -> Unit
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunctionWithoutReturn("self", func)
        .build()
)

fun KnolusContext.registerMemberFunction(
    typeName: String,
    functionName: String,
    parameterName: String,
    func: suspend (context: KnolusContext, self: VariableValue, parameter: VariableValue) -> VariableValue
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction("self", parameterName, func)
        .build()
)

fun KnolusContext.registerMemberFunctionWithoutReturn(
    typeName: String,
    functionName: String,
    parameterName: String,
    func: suspend (context: KnolusContext, self: VariableValue, parameter: VariableValue) -> Unit
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunctionWithoutReturn("self", parameterName, func)
        .build()
)

fun KnolusContext.registerMemberFunction(
    typeName: String,
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    func: suspend (context: KnolusContext, self: VariableValue, firstParameter: VariableValue, secondParameter: VariableValue) -> VariableValue
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction("self", firstParameterName, secondParameterName, func)
        .build()
)

fun KnolusContext.registerMemberFunctionWithoutReturn(
    typeName: String,
    functionName: String,
    firstParameterName: String,
    secondParameterName: String,
    func: suspend (context: KnolusContext, self: VariableValue, firstParameter: VariableValue, secondParameter: VariableValue) -> Unit
) = register(
    "Get${typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunctionWithoutReturn("self", firstParameterName, secondParameterName, func)
        .build()
)

fun <P0> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    func: suspend (context: KnolusContext, self: P0) -> VariableValue
) = register(
    "Get${typeSpec.typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction(typeSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    parameterSpec: ParameterSpec<P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> VariableValue
) = register(
    "Get${typeSpec.typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    parameterSpec: ParameterSpec<P1>,
    func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit
) = register(
    "Get${typeSpec.typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunctionWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunction(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> VariableValue
) = register(
    "Get${typeSpec.typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <P0, P1, P2> KnolusContext.registerMemberFunctionWithoutReturn(
    typeSpec: TypeParameterSpec<P0>,
    functionName: String,
    firstParameterSpec: ParameterSpec<P1>,
    secondParameterSpec: ParameterSpec<P2>,
    func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit
) = register(
    "Get${typeSpec.typeName}MemberFunction_${functionName}",
    functionBuilder()
        .setFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

/** Getter */

fun KnolusContext.registerMemberPropertyGetter(
    typeName: String,
    functionName: String,
    func: suspend (context: KnolusContext, self: VariableValue) -> VariableValue
) = register(
    "Get${typeName}MemberProperty_${functionName}",
    functionBuilder()
        .setFunction("self", func)
        .build()
)

fun <P0> KnolusContext.registerMemberPropertyGetter(
    typeSpec: TypeParameterSpec<P0>,
    propertyName: String,
    func: suspend (context: KnolusContext, self: P0) -> VariableValue
) = register(
    "Get${typeSpec.typeName}MemberProperty_${propertyName}",
    functionBuilder()
        .setFunction(typeSpec, func)
        .build()
)