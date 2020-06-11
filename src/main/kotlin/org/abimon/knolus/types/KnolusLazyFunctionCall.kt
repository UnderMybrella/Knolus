package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext

@ExperimentalUnsignedTypes
data class KnolusLazyFunctionCall(val name: String, val parameters: Array<KnolusUnion.FunctionParameterType>) :
    KnolusTypedValue.RuntimeValue<KnolusTypedValue>, KnolusUnion.Action<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall>
        get() = TypeInfo

    override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> =
        run(context)

    override suspend fun <T> run(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> {
        return parameters.fold(KnolusResult.foldingMutableListOf<KnolusUnion.FunctionParameterType>()) { acc, funcParam ->
            acc.flatMapOrSelf { list ->
                if (funcParam.parameter is KnolusTypedValue.UnsureValue<*> && funcParam.parameter.needsEvaluation(context))
                    funcParam.parameter.evaluate(context).map { value -> list.withElement(KnolusUnion.FunctionParameterType(funcParam.name, value)) }
                else
                    KnolusResult.success(list.withElement(funcParam))
            }
        }.flatMap { params -> context.invokeFunction(name, params.toTypedArray()) }
    }
}