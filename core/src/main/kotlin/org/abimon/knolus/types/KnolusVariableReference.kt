package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

/**
 * A reference to a variable defined by either the environment, or the user.
 * Note: These are 'freestanding' variables, not member properties.
 */
data class KnolusVariableReference(val variableName: String) : KnolusTypedValue.RuntimeValue<KnolusTypedValue> {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference> {
        override val typeHierarchicalNames: Array<String> = arrayOf("VariableReference", "Reference", "Object")
        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusVariableReference
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusVariableReference>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> = context[variableName]
}