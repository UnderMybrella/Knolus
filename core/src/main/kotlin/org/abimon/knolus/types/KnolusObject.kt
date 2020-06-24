package org.abimon.knolus.types

object KnolusObject : KnolusTypedValue.TypeInfo<KnolusTypedValue> {
    override val typeHierarchicalNames: Array<String> = arrayOf("Object")

    override fun isInstance(instance: Any?): Boolean = instance is KnolusTypedValue
    override fun asInstance(instance: Any?): KnolusTypedValue = instance as KnolusTypedValue
    override fun asInstanceSafe(instance: Any?): KnolusTypedValue? = instance as? KnolusTypedValue
}