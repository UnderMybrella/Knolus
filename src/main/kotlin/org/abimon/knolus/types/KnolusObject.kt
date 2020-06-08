package org.abimon.knolus.types

object KnolusObject : KnolusTypedValue.TypeInfo<KnolusTypedValue> {
    override val typeHierarchicalNames: Array<String> = arrayOf("Object")

    override fun isInstance(value: KnolusTypedValue): Boolean = true
}