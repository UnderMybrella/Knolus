package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap

data class KnolusPropertyReference(val variableName: String, val propertyName: String) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("MemberVariableReference", "Reference", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusPropertyReference
        override fun asInstance(instance: Any?): KnolusPropertyReference = instance as KnolusPropertyReference
        override fun asInstanceSafe(instance: Any?): KnolusPropertyReference? = instance as? KnolusPropertyReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusPropertyReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun evaluate(context: KnolusContext): KorneaResult<KnolusTypedValue> = context[variableName].flatMap { member ->
        context.invokeMemberPropertyGetter(member, propertyName)
    }
}