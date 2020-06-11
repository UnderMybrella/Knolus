package org.abimon.knolus.types

import org.abimon.knolus.KnolusResult
import org.abimon.knolus.context.KnolusContext

data class KnolusBoolean(val boolean: Boolean) : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Boolean", "Object")
        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusBoolean
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean>
        get() = TypeInfo

    override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = KnolusResult.success(boolean.toString())
    override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = KnolusResult.success(if (boolean) 1 else 0)
    override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = KnolusResult.success(boolean)
}