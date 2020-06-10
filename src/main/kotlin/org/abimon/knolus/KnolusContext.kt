@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.abimon.knolus

import org.abimon.knolus.modules.functionregistry.functionBuilder
import org.abimon.knolus.modules.functionregistry.setFunction
import org.abimon.knolus.restrictions.KnolusRestrictions
import org.abimon.knolus.restrictions.canAskAsParentForFunction
import org.abimon.knolus.restrictions.canAskAsParentForVariable
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
open class KnolusContext(val parent: KnolusContext?, val restrictions: KnolusRestrictions) {
    companion object {
        const val ACCESS_DENIED = 0x1000
        const val UNDECLARED_VARIABLE = 0x1001
        const val UNDECLARED_FUNCTION = 0x1002

        const val FAILED_TO_GET_VARIABLE = 0x1100
        const val FAILED_TO_SET_VARIABLE = 0x1101
        const val FAILED_TO_REGISTER_FUNCTION = 0x1102
//        const val
    }

    val recursionLevel: Int by lazy { if (parent == null) 0 else parent.recursionLevel + 1 }

    protected val variableRegistry: MutableMap<String, KnolusTypedValue> = HashMap()
    protected val functionRegistry: MutableMap<String, MutableList<KnolusFunction<KnolusTypedValue?>>> =
        HashMap()

    operator fun get(key: String): KnolusTypedValue? =
        if (restrictions.canGetVariable(this, key) && key in variableRegistry) variableRegistry[key]
        else if (parent?.canAskAsParentForVariable(this, key) == true) parent[key]
        else null

    operator fun set(key: String, global: Boolean = false, value: KnolusTypedValue): KnolusResult<KnolusTypedValue?> {
        val parentResult: KnolusResult<KnolusTypedValue?>?
        if (global && parent != null) {
            parentResult = parent.set(key, global, value)
            if (parentResult is KnolusResult.Successful) return parentResult
        } else {
            parentResult = null
        }

        if (restrictions.canSetVariable(this, key, value, parentResult != null)) {
            return KnolusResult.Success(variableRegistry.put(key, value))
        } else {
            return KnolusResult.Error(FAILED_TO_SET_VARIABLE, "Failed to locally set $key to $value", parentResult)
        }
    }

    operator fun contains(key: String): Boolean = (restrictions.canGetVariable(this, key) && key in variableRegistry)
            || (parent?.canAskAsParentForVariable(this, key) == true && parent.contains(key))

    fun register(name: String, func: KnolusFunction<KnolusTypedValue?>, global: Boolean = false): KnolusResult<Array<KnolusFunction<KnolusTypedValue?>>> {
        val sanitisedName = name.sanitiseFunctionIdentifier()

        val parentResult: KnolusResult<Array<KnolusFunction<KnolusTypedValue?>>>?
        if (global && parent != null) {
            parentResult = parent.register(sanitisedName, func, global)
            if (parentResult is KnolusResult.Successful) return parentResult
        } else {
            parentResult = null
        }

        if (restrictions.canRegisterFunction(this, sanitisedName, func, parentResult)) {
            val functions: MutableList<KnolusFunction<KnolusTypedValue?>>
            if (sanitisedName !in functionRegistry) {
                functions = ArrayList()
                functionRegistry[sanitisedName] = functions
            } else {
                functions = functionRegistry.getValue(sanitisedName)
            }

            functions.add(func)

            return KnolusResult.Success(functions.toTypedArray())
        } else {
            return KnolusResult.Error(FAILED_TO_REGISTER_FUNCTION, "Failed to register $name ($func)", parentResult)
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
    ): KnolusResult<KnolusTypedValue> {
        if (!restrictions.canAskForMemberFunction(this, member, functionName, functionParameters))
            return KnolusResult.Error(ACCESS_DENIED, "KnolusRestriction denied access to member function")

        val params = arrayOfNulls<KnolusUnion.FunctionParameterType>(functionParameters.size + 1)
        params[0] = KnolusUnion.FunctionParameterType("self", member)
        functionParameters.copyInto(params, 1)

        @Suppress("UNCHECKED_CAST")
        params as Array<KnolusUnion.FunctionParameterType>

        return member.typeInfo.getMemberFunctionNames(functionName).fold(KnolusResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun invokeMemberPropertyGetter(
        member: KnolusTypedValue,
        propertyName: String,
    ): KnolusResult<KnolusTypedValue> {
        if (!restrictions.canAskForMemberPropertyGetter(this, member, propertyName))
            return KnolusResult.Error(ACCESS_DENIED, "KnolusRestriction denied access to member property getter")

        val params = arrayOf(KnolusUnion.FunctionParameterType("self", member))

        return member.typeInfo.getMemberPropertyGetterNames(propertyName).fold(KnolusResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun invokeOperator(
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KnolusResult<KnolusTypedValue> {
        if (!restrictions.canAskForOperatorFunction(this, operator, a, b))
            return KnolusResult.Error(ACCESS_DENIED, "KnolusRestriction denied access to operator function")

        val params = arrayOf(KnolusUnion.FunctionParameterType("a", a), KnolusUnion.FunctionParameterType("b", b))

        return a.typeInfo.getMemberOperatorNames(operator).fold(KnolusResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun invokeFunction(
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<KnolusTypedValue> {
        if (!restrictions.canAskForFunction(this, functionName, functionParameters))
            return KnolusResult.Error(ACCESS_DENIED, "KnolusRestriction denied access to function")

        var functionRunDenied = false

        functionRegistry[functionName.sanitiseFunctionIdentifier()]?.forEach functionList@{ function ->
            if (!restrictions.canTryFunction(this, functionName, functionParameters, function))
                return@functionList

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

            unmappedParams.forEach paramList@{ param ->
                when (param.missingPolicy) {
                    KnolusFunctionParameterMissingPolicy.Mandatory -> return@functionList
                    KnolusFunctionParameterMissingPolicy.Optional -> return@paramList
                    is KnolusFunctionParameterMissingPolicy.Substitute<*> ->
                        passedParams.putIfAbsent(param.name, param.missingPolicy.default)
                }
            }

            if (!restrictions.canRunFunction(this, function, passedParams)) {
                functionRunDenied = true
                return@functionList
            }
            val subroutineRestrictions = restrictions.createSubroutineRestrictions(this, function, passedParams)

            return KnolusResult.Success(function.suspendInvoke(KnolusContext(this, subroutineRestrictions), passedParams)
                ?: KnolusConstants.Null)
        }

        if (parent?.canAskAsParentForFunction(this, functionName, functionParameters) == true)
            return parent.invokeFunction(functionName, functionParameters)
        if (functionRunDenied)
            return KnolusResult.Error(ACCESS_DENIED, "KnolusRestriction denied access to function")

        return KnolusResult.empty()
    }
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