package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.toFormattedBoolean
import dev.brella.knolus.toIntOrNullBaseN
import org.abimon.kornea.errors.common.KorneaResult

inline class KnolusString(val string: String) : KnolusTypedValue {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("String", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusString
        override fun asInstance(instance: Any?): KnolusString = instance as KnolusString
        override fun asInstanceSafe(instance: Any?): KnolusString? = instance as? KnolusString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusString>
        get() = TypeInfo

    override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
        when (typeInfo) {
            KnolusString -> typeInfo.asResult(this)
            KnolusDouble -> typeInfo.asResultOrEmpty(string.toDoubleOrNull()?.let(::KnolusDouble))
            KnolusInt -> typeInfo.asResultOrEmpty(string.toIntOrNullBaseN()?.let(::KnolusInt))
            KnolusChar -> typeInfo.asResultOrEmpty(string.firstOrNull()?.let(::KnolusChar))
            KnolusNumericalType -> typeInfo.asResultOrEmpty(if (string.contains('.')) string.toDoubleOrNull()?.let(::KnolusDouble) else string.toIntOrNullBaseN()?.let(::KnolusInt))
            KnolusBoolean -> typeInfo.asResult(KnolusBoolean(string.toFormattedBoolean()))

            else -> KorneaResult.empty()
        }
}