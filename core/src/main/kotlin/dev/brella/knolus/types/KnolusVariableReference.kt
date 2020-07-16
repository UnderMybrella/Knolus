package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

/**
 * A reference to a variable defined by either the environment, or the user.
 * Note: These are 'freestanding' variables, not member properties.
 */
data class KnolusVariableReference(val variableName: String) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("VariableReference", "Reference", "Object")
        override fun isInstance(instance: Any?): Boolean = instance is KnolusVariableReference
        override fun asInstance(instance: Any?): KnolusVariableReference = instance as KnolusVariableReference
        override fun asInstanceSafe(instance: Any?): KnolusVariableReference? = instance as? KnolusVariableReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> = context[variableName]
}