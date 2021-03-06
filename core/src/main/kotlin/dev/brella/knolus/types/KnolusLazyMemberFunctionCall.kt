package dev.brella.knolus.types

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.errors.common.switchIfEmpty

@ExperimentalUnsignedTypes
data class KnolusLazyMemberFunctionCall(
    val variableName: String,
    val functionName: String,
    val parameters: Array<KnolusUnion.FunctionParameterType>,
) : KnolusTypedValue.RuntimeValue<KnolusTypedValue>, KnolusUnion.Action<KnolusTypedValue> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall> {
        override val typeHierarchicalNames: Array<String> = arrayOf("FunctionCall", "Reference", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusLazyMemberFunctionCall
        override fun asInstance(instance: Any?): KnolusLazyMemberFunctionCall = instance as KnolusLazyMemberFunctionCall
        override fun asInstanceSafe(instance: Any?): KnolusLazyMemberFunctionCall? = instance as? KnolusLazyMemberFunctionCall
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyMemberFunctionCall>
        get() = TypeInfo

    override suspend fun evaluate(context: KnolusContext): KorneaResult<KnolusTypedValue> =
        run(context)

    override suspend fun run(context: KnolusContext): KorneaResult<KnolusTypedValue> =
        context[variableName].switchIfEmpty { empty ->
            KorneaResult.errorAsIllegalState(KnolusContext.UNDECLARED_VARIABLE, "No such variable by name of $variableName", empty)
        }.flatMap { member ->
            parameters.fold(KorneaResult.foldingMutableListOf<KnolusUnion.FunctionParameterType>()) { acc, funcParam ->
                acc.flatMap { list ->
                    if (funcParam.parameter is KnolusTypedValue.UnsureValue<*> && funcParam.parameter.needsEvaluation(context)) {
                        funcParam.parameter.evaluate(context).map { value ->
                            list.withElement(KnolusUnion.FunctionParameterType(funcParam.name, value))
                        }
                    } else {
                        KorneaResult.success(list.withElement(funcParam))
                    }
                }
            }.flatMap { params -> context.invokeMemberFunction(member, functionName, params.toTypedArray()) }
        }
}