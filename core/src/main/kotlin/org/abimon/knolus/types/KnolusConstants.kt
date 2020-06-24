package org.abimon.knolus.types

import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult

sealed class KnolusConstants {
    object Null : KnolusTypedValue, KnolusTypedValue.TypeInfo<Null> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Null", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Null>
            get() = this

        override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = if (context.nullTypeCoercible) KorneaResult.success(0) else KorneaResult.Empty.ofNull()
        override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = if (context.nullTypeCoercible) KorneaResult.success("[null]") else KorneaResult.Empty.ofNull()
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = if (context.nullTypeCoercible) KorneaResult.success(false) else KorneaResult.Empty.ofNull()

        override fun isInstance(instance: Any?): Boolean = instance === Null
        override fun asInstance(instance: Any?): Null = instance as Null
        override fun asInstanceSafe(instance: Any?): Null? = instance as? Null
    }

    object Undefined : KnolusTypedValue, KnolusTypedValue.TypeInfo<Undefined> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Undefined", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Undefined>
            get() = this

        override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = if (context.undefinedTypeCoercible) KorneaResult.success(0) else KorneaResult.Empty.ofUndefined()
        override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = if (context.undefinedTypeCoercible) KorneaResult.success("[undefined]") else KorneaResult.Empty.ofUndefined()
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = if (context.undefinedTypeCoercible) KorneaResult.success(false) else KorneaResult.Empty.ofUndefined()

        override fun isInstance(instance: Any?): Boolean = instance === Undefined
        override fun asInstance(instance: Any?): Undefined = instance as Undefined
        override fun asInstanceSafe(instance: Any?): Undefined? = instance as? Undefined
    }
}