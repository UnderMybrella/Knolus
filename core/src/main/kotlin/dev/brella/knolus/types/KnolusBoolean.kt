package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult

inline class KnolusBoolean(val boolean: Boolean) : KnolusTypedValue {
    companion object TypeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Boolean", "Object")
        override fun isInstance(instance: Any?): Boolean = instance is KnolusBoolean
        override fun asInstance(instance: Any?): KnolusBoolean = instance as KnolusBoolean
        override fun asInstanceSafe(instance: Any?): KnolusBoolean? = instance as? KnolusBoolean
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusBoolean>
        get() = TypeInfo

    override suspend fun <R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> =
        when (typeInfo) {
            KnolusString -> typeInfo.asResult(KnolusString(boolean.toString()))
            KnolusNumericalType -> typeInfo.asResult(KnolusInt(if (boolean) 1 else 0))
            KnolusBoolean -> typeInfo.asResult(this)

            else -> KorneaResult.empty()
        }
}