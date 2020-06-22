package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.flatMap

data class KnolusPropertyReference(val variableName: String, val propertyName: String) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("MemberVariableReference", "Reference", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusPropertyReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> = context[variableName].flatMap { member ->
        context.invokeMemberPropertyGetter(member, propertyName)
    }

    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = evaluate(context).flatMap { it.asString(context) }
    override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = evaluate(context).flatMap { it.asNumber(context) }
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = evaluate(context).flatMap { it.asBoolean(context) }
}