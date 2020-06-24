package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.errors.common.map

data class KnolusLazyString(val components: Array<KnolusUnion.StringComponent>) :
    KnolusTypedValue.RuntimeValue<KnolusString> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("LazyString", "Object")

        override fun isInstance(instance: Any?): Boolean = instance is KnolusLazyString
        override fun asInstance(instance: Any?): KnolusLazyString = instance as KnolusLazyString
        override fun asInstanceSafe(instance: Any?): KnolusLazyString? = instance as? KnolusLazyString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyString>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<KnolusString> =
        components.fold(KorneaResult.foldingMutableListOf<String>()) { acc, component ->
            acc.flatMap { list ->
                when (component) {
                    is KnolusUnion.StringComponent.RawText ->
                        KorneaResult.success(list.withElement(component.text))
                    is KnolusUnion.StringComponent.VariableReference ->
                        context[component.variableName].flatMap { value ->
                            value.asString(context).map(list::withElement)
                        }
                }
            }
        }.map { list -> KnolusString(list.joinToString("")) }

    override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> =
        evaluate(context).map(KnolusString::string)

    override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> =
        evaluate(context).flatMap { knolusString ->
            val str = knolusString.string
            if (str.contains('.'))
                KorneaResult.success(str.toDouble())
            else
                KorneaResult.success(str.toIntBaseN())
        }

    override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> =
        evaluate(context).flatMap { knolusString -> KorneaResult.success(knolusString.string.toFormattedBoolean()) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnolusLazyString

        if (!components.contentEquals(other.components)) return false

        return true
    }

    override fun hashCode(): Int {
        return components.contentHashCode()
    }
}