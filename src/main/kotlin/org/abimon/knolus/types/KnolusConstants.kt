package org.abimon.knolus.types

import org.abimon.knolus.KnolusContext

sealed class KnolusConstants {

    object Null : KnolusTypedValue, KnolusTypedValue.TypeInfo<Null> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Null", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Null>
            get() = this

        override suspend fun asNumber(context: KnolusContext): Number = 0
        override suspend fun asString(context: KnolusContext): String = "[null]"
        override suspend fun asBoolean(context: KnolusContext): Boolean = false
    }

    object Undefined : KnolusTypedValue, KnolusTypedValue.TypeInfo<Undefined> {
        override val typeHierarchicalNames: Array<String> = arrayOf("Undefined", "Constant", "Object")
        override val typeInfo: KnolusTypedValue.TypeInfo<Undefined>
            get() = this

        override suspend fun asNumber(context: KnolusContext): Number = 0
        override suspend fun asString(context: KnolusContext): String = "[undefined]"
        override suspend fun asBoolean(context: KnolusContext): Boolean = false
    }
}