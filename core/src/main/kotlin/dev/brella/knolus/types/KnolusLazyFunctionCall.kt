package dev.brella.knolus.types

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map

@ExperimentalUnsignedTypes
data class KnolusLazyFunctionCall(val name: String, val parameters: Array<KnolusUnion.FunctionParameterType>) :
    KnolusTypedValue.RuntimeValue<KnolusTypedValue>, KnolusUnion.Action<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusLazyFunctionCall
        override fun asInstance(instance: Any?): KnolusLazyFunctionCall = instance as KnolusLazyFunctionCall
        override fun asInstanceSafe(instance: Any?): KnolusLazyFunctionCall? = instance as? KnolusLazyFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyFunctionCall>
        get() = TypeInfo

    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> =
        run(context)

    override suspend fun <T> run(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> {
        return parameters.fold(KorneaResult.foldingMutableListOf<KnolusUnion.FunctionParameterType>()) { acc, funcParam ->
            acc.flatMap { list ->
                if (funcParam.parameter is KnolusTypedValue.UnsureValue<*> && funcParam.parameter.needsEvaluation(context))
                    funcParam.parameter.evaluate(context).map { value ->
                        list.withElement(KnolusUnion.FunctionParameterType(funcParam.name, value))
                    }
                else
                    KorneaResult.success(list.withElement(funcParam))
            }
        }.flatMap { params -> context.invokeFunction(name, params.toTypedArray()) }
    }
}