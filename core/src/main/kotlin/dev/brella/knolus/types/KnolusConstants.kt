package dev.brella.knolus.types

import dev.brella.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

sealed class KnolusConstants {
    object Null : KnolusTypedValue, KnolusTypedValue.TypeInfo<Null> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Null", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Null>
            get() = this

        override fun isInstance(instance: Any?): Boolean = instance === Null
        override fun asInstance(instance: Any?): Null = instance as Null
        override fun asInstanceSafe(instance: Any?): Null? = instance as? Null

        override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
            if (!context.nullTypeCoercible) KorneaResult.Empty.ofNull()
            else {
                when (typeInfo) {
                    KnolusString -> typeInfo.asResult(KnolusString("[null]"))
                    KnolusNumericalType -> typeInfo.asResult(KnolusInt(0))
                    KnolusBoolean -> typeInfo.asResult(KnolusBoolean(false))

                    else -> KorneaResult.empty()
                }
            }
    }

    object Undefined : KnolusTypedValue, KnolusTypedValue.TypeInfo<Undefined> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Undefined", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Undefined>
            get() = this

        override fun isInstance(instance: Any?): Boolean = instance === Undefined
        override fun asInstance(instance: Any?): Undefined = instance as Undefined
        override fun asInstanceSafe(instance: Any?): Undefined? = instance as? Undefined

        override suspend fun <T, R: KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> asTypeImpl(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> =
            if (!context.undefinedTypeCoercible) KorneaResult.Empty.ofUndefined()
            else {
                when (typeInfo) {
                    KnolusString -> typeInfo.asResult(KnolusString("[null]"))
                    KnolusNumericalType -> typeInfo.asResult(KnolusInt(0))
                    KnolusBoolean -> typeInfo.asResult(KnolusBoolean(false))

                    else -> KorneaResult.empty()
                }
            }
    }
}