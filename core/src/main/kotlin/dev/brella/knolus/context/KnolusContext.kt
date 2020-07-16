@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.brella.knolus.context

import dev.brella.knolus.*
import dev.brella.knolus.restrictions.KnolusRestriction
import dev.brella.knolus.restrictions.canAskAsParentForFunction
import dev.brella.knolus.restrictions.canAskAsParentForVariable
import dev.brella.knolus.types.*
import org.abimon.kornea.errors.common.*
import org.kornea.toolkit.common.filterToInstance
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@ExperimentalUnsignedTypes
abstract class KnolusContext<R>(val parent: KnolusContext<R>?, val restrictions: KnolusRestriction<R>) {
    companion object {
        const val ACCESS_DENIED = 0x1000
        const val UNDECLARED_VARIABLE = 0x1001
        const val UNDECLARED_FUNCTION = 0x1002

        const val FAILED_TO_GET_VARIABLE = 0x1100
        const val FAILED_TO_SET_VARIABLE = 0x1101
        const val FAILED_TO_REGISTER_FUNCTION = 0x1102
//        const val
    }

    val nullTypeCoercible: Boolean = true
    val undefinedTypeCoercible: Boolean = false

    val depth: Int by lazy { if (parent == null) 0 else parent.depth + 1 }

    open fun recursiveCountFor(func: KnolusFunction<*, R, *>): Int = parent?.recursiveCountFor(func) ?: 0

    protected val variableRegistry: MutableMap<String, KnolusTypedValue> = HashMap()
    protected val functionRegistry: MutableMap<String, MutableList<KnolusFunction<KnolusTypedValue?, R, *>>> =
        HashMap()

    operator fun get(key: String): KorneaResult<KnolusTypedValue> =
        restrictions.canGetVariable(this, key).flatMap {
            val value = variableRegistry[key]

            if (value == null) {
                parent?.canAskAsParentForVariable(this, key)?.flatMap { parent[key] } ?: KorneaResult.empty()
            } else {
                KorneaResult.successInline(value)
            }
        }

    operator fun set(key: String, global: Boolean = false, value: KnolusTypedValue): KorneaResult<KnolusTypedValue?> {
        val parentResult: KorneaResult<KnolusTypedValue?>?
        if (global && parent != null) {
            parentResult = parent.set(key, global, value)
            if (parentResult is KorneaResult.Success) return parentResult
        } else {
            parentResult = null
        }

        return restrictions.canSetVariable(this, key, value, parentResult)
            .flatMap { KorneaResult.successInline(variableRegistry.put(key, value)) }
            .switchIfHasCause { result ->
                KorneaResult.errorAsIllegalArgument(
                    FAILED_TO_SET_VARIABLE,
                    "Failed to locally set $key to $value",
                    result.mapFailureRootCause(parentResult)
                )
            }
    }

    fun containsWithResult(key: String): KorneaResult<R> =
        restrictions.canGetVariable(this, key).filter { key in variableRegistry }
            .switchIfFailure {
                parent?.canAskAsParentForVariable(this, key)?.filter { parent.contains(key) } ?: KorneaResult.empty()
            }

    operator fun contains(key: String): Boolean =
        containsWithResult(key) is KorneaResult.Success

    fun register(
        name: String,
        func: KnolusFunction<KnolusTypedValue?, R, *>,
        global: Boolean = false,
    ): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> {
        val sanitisedName = name.sanitiseFunctionIdentifier()

        val parentResult: KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>>?
        if (global && parent != null) {
            parentResult = parent.register(sanitisedName, func, global)
            if (parentResult is KorneaResult.Success) return parentResult
        } else {
            parentResult = null
        }

        return restrictions.canRegisterFunction(this, sanitisedName, func, parentResult)
            .map { func.addTo(functionRegistry.computeIfAbsent(sanitisedName) { ArrayList() }) }
            .switchIfHasCause { result ->
                KorneaResult.errorAsIllegalState(
                    FAILED_TO_REGISTER_FUNCTION,
                    "Failed to register $name ($func)",
                    result.mapFailureRootCause(parentResult)
                )
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
    ): KorneaResult<KnolusTypedValue> {
        restrictions.canAskForMemberFunction(this, member, functionName, functionParameters).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to member function", causedBy)
        }

        val params = arrayOfNulls<KnolusUnion.FunctionParameterType>(functionParameters.size + 1)
        params[0] = KnolusUnion.FunctionParameterType("self", member)
        functionParameters.copyInto(params, 1)

        @Suppress("UNCHECKED_CAST")
        params as Array<KnolusUnion.FunctionParameterType>

        return member.typeInfo.getMemberFunctionNames(functionName).fold(KorneaResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun invokeMemberPropertyGetter(
        member: KnolusTypedValue,
        propertyName: String,
    ): KorneaResult<KnolusTypedValue> {
        restrictions.canAskForMemberPropertyGetter(this, member, propertyName).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to member property", causedBy)
        }

        val params = arrayOf(KnolusUnion.FunctionParameterType("self", member))

        return member.typeInfo.getMemberPropertyGetterNames(propertyName).fold(KorneaResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun invokeOperator(
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KorneaResult<KnolusTypedValue> {
        restrictions.canAskForOperatorFunction(this, operator, a, b).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to operator function", causedBy)
        }

        val params = arrayOf(
            KnolusUnion.FunctionParameterType("a", a),
            KnolusUnion.FunctionParameterType("b", b)
        )

        return a.typeInfo.getMemberOperatorNames(operator).fold(KorneaResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params) }
        }
    }

    suspend fun <T: KnolusTypedValue> invokeCastingOperator(
        self: KnolusTypedValue,
        castingTo: KnolusTypedValue.TypeInfo<T>
    ): KorneaResult<T> {
        restrictions.canAskForCastingOperatorFunction(this, self, castingTo).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to casting operator function", causedBy)
        }

        val params = arrayOf(KnolusUnion.FunctionParameterType("self", self))

        return self.typeInfo.getMemberCastingOperatorNames(castingTo.typeName).fold(KorneaResult.empty()) { acc, funcName ->
            acc.switchIfEmpty { invokeFunction(funcName, params).filterToInstance(castingTo) }
        }
    }

    suspend fun invokeFunction(
        functionName: String,
        vararg parameters: Pair<String, KnolusTypedValue>,
    ): KorneaResult<KnolusTypedValue> =
        invokeFunction(functionName, parameters.mapToArray { (k, v) ->
            KnolusUnion.FunctionParameterType(k, v)
        })

    suspend fun invokeFunction(
        functionName: String,
        vararg parameters: KnolusTypedValue,
    ): KorneaResult<KnolusTypedValue> =
        invokeFunction(functionName, parameters.mapToArray { param ->
            KnolusUnion.FunctionParameterType(null, param)
        })

    suspend fun invokeFunction(
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        selfContext: KnolusContext<R> = this,
    ): KorneaResult<KnolusTypedValue> {
        restrictions.canAskForFunction(this, functionName, functionParameters).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to function", causedBy)
        }

        var collectiveCause: KorneaResult<KnolusTypedValue>? = null

        functionRegistry[functionName.sanitiseFunctionIdentifier()]?.forEach functionList@{ function ->
            restrictions.canTryFunction(this, functionName, functionParameters, function).doOnFailure { causedBy ->
                collectiveCause = (collectiveCause as? KorneaResult.WithCause)?.withCause(causedBy) ?: collectiveCause
            }

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
                        passedParams.putIfAbsent(param.name, (param.missingPolicy as KnolusFunctionParameterMissingPolicy.Substitute<*>).default)
                }
            }

            val result = selfContext.invokeFunction(function, passedParams)
            if (result is KorneaResult.Success) return result
            if (collectiveCause == null)
                collectiveCause = result
            else
                collectiveCause = (collectiveCause as? KorneaResult.WithCause)?.withCause(result as KorneaResult.Failure) ?: collectiveCause
//
//            if (!restrictions.canRunFunction(this, function, passedParams)) {
//                functionRunDenied = true
//                return@functionList
//            }
//            val subroutineRestrictions = restrictions.createSubroutineRestrictions(this, function, passedParams)
//
//            return KorneaResult.Success(
//                function.suspendInvoke(KnolusFunctionContext(function, this, subroutineRestrictions), passedParams)
//                    ?: KnolusConstants.Null
//            )
        }

        if (parent == null) return collectiveCause ?: KorneaResult.empty()

        return parent.canAskAsParentForFunction(this, functionName, functionParameters).flatMap {
            parent.invokeFunction(functionName, functionParameters, selfContext)
        }
    }

    suspend fun invokeFunction(
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        vararg parameters: Pair<String, KnolusTypedValue>,
    ) = invokeFunction(function, parameters.toMap())

    suspend fun invokeFunction(
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KorneaResult<KnolusTypedValue> {
        restrictions.canRunFunction(this, function, parameters).doOnFailure { causedBy ->
            return KorneaResult.errorAsIllegalState(ACCESS_DENIED, "KnolusRestriction denied access to function", causedBy)
        }

        return restrictions.createSubroutineRestrictions(this, function, parameters)
            .flatMap { subroutineRestrictions ->
                function as KnolusFunction<KnolusTypedValue?, R, KnolusContext<out R>>

                KorneaResult.successInline(
                    function.suspendInvoke(KnolusFunctionContext(function, this, subroutineRestrictions), parameters)
                    ?: KnolusConstants.Null
                )
            }
    }
}