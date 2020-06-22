package org.abimon.knolus

import org.abimon.knolus.types.KnolusLazyFunctionCall
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

@ExperimentalUnsignedTypes
class KnolusFunctionCallBuilder(val functionName: String) {
    val parameters: MutableList<KnolusUnion.FunctionParameterType> = ArrayList()

    fun param(name: String? = null, parameter: KnolusTypedValue) = param(KnolusUnion.FunctionParameterType(name, parameter))
    fun param(parameter: KnolusUnion.FunctionParameterType): KnolusFunctionCallBuilder {
        parameters.add(parameter)
        return this
    }

    fun allParams(vararg parameters: KnolusTypedValue): KnolusFunctionCallBuilder {
        this.parameters.addAll(parameters.mapToArray { KnolusUnion.FunctionParameterType(null, it) })
        return this
    }

    fun allParams(vararg parameters: Pair<String?, KnolusTypedValue>): KnolusFunctionCallBuilder {
        this.parameters.addAll(parameters.mapToArray { (a, b) -> KnolusUnion.FunctionParameterType(a, b) })
        return this
    }

    fun build(): KnolusLazyFunctionCall = KnolusLazyFunctionCall(functionName, parameters.toTypedArray())
    fun buildVar(): KnolusUnion.VariableValue<KnolusLazyFunctionCall> = KnolusUnion.VariableValue.Lazy(KnolusLazyFunctionCall(functionName, parameters.toTypedArray()))

    fun buildResult(): KorneaResult<KnolusLazyFunctionCall> = KorneaResult.success(KnolusLazyFunctionCall (functionName, parameters.toTypedArray()))
    fun buildVarResult(): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> = KorneaResult.success(KnolusUnion.VariableValue.Lazy(KnolusLazyFunctionCall(functionName, parameters.toTypedArray())))
}

inline fun buildFunctionCall(name: String, init: KnolusFunctionCallBuilder.() -> KnolusFunctionCallBuilder): KnolusLazyFunctionCall =
    KnolusFunctionCallBuilder(name).init().build()

inline fun buildFunctionCallAsVar(name: String, init: KnolusFunctionCallBuilder.() -> KnolusFunctionCallBuilder): KnolusUnion.VariableValue<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).init().buildVar()

inline fun buildFunctionCallAsResult(name: String, init: KnolusFunctionCallBuilder.() -> KnolusFunctionCallBuilder): KorneaResult<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).init().buildResult()

inline fun buildFunctionCallAsVarResult(name: String, init: KnolusFunctionCallBuilder.() -> KnolusFunctionCallBuilder): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> =
    KnolusFunctionCallBuilder(name).init().buildVarResult()

/** */

inline fun buildFunctionCall(name: String): KnolusLazyFunctionCall =
    KnolusFunctionCallBuilder(name).build()

inline fun buildFunctionCallAsVar(name: String): KnolusUnion.VariableValue<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).buildVar()

inline fun buildFunctionCallAsResult(name: String): KorneaResult<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).buildResult()

inline fun buildFunctionCallAsVarResult(name: String): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> =
    KnolusFunctionCallBuilder(name).buildVarResult()

/** */

inline fun buildFunctionCall(name: String, vararg params: KnolusTypedValue): KnolusLazyFunctionCall =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).build()

inline fun buildFunctionCallAsVar(name: String, vararg params: KnolusTypedValue): KnolusUnion.VariableValue<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildVar()

inline fun buildFunctionCallAsResult(name: String, vararg params: KnolusTypedValue): KorneaResult<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildResult()

inline fun buildFunctionCallAsVarResult(name: String, vararg params: KnolusTypedValue): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildVarResult()

/** */
inline fun buildFunctionCall(name: String, vararg params: Pair<String?, KnolusTypedValue>): KnolusLazyFunctionCall =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).build()

inline fun buildFunctionCallAsVar(name: String, vararg params: Pair<String?, KnolusTypedValue>): KnolusUnion.VariableValue<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildVar()

inline fun buildFunctionCallAsResult(name: String, vararg params: Pair<String?, KnolusTypedValue>): KorneaResult<KnolusLazyFunctionCall> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildResult()

inline fun buildFunctionCallAsVarResult(name: String, vararg params: Pair<String?, KnolusTypedValue>): KorneaResult<KnolusUnion.VariableValue<KnolusLazyFunctionCall>> =
    KnolusFunctionCallBuilder(name).allParams(parameters = params).buildVarResult()