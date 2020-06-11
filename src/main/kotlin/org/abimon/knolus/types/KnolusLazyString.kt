package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext

data class KnolusLazyString(val components: Array<KnolusUnion.StringComponent>) :
    KnolusTypedValue.RuntimeValue<KnolusString> {
    companion object TypeInfo : KnolusTypedValue.TypeInfo<KnolusLazyString> {
        override val typeHierarchicalNames: Array<String> = arrayOf("LazyString", "Object")

        override fun isInstance(value: KnolusTypedValue): Boolean = value is KnolusLazyString
    }

    override val typeInfo: KnolusTypedValue.TypeInfo<KnolusLazyString>
        get() = TypeInfo

    @ExperimentalUnsignedTypes
    override suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<KnolusString> {
        val initial: KnolusResult<MutableList<String>> = KnolusResult.success(ArrayList())

        return components.fold(initial) { acc, component ->
            acc.flatMapOrSelf { list ->
                when (component) {
                    is KnolusUnion.StringComponent.RawText ->
                        KnolusResult.success(list.withElement(component.text))
                    is KnolusUnion.StringComponent.VariableReference ->
                        context[component.variableName].flatMap { value ->
                            value.asString(context).map(list::withElement)
                        }
                }
            }
        }.map { list -> KnolusString(list.joinToString("")) }
    }

    override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> =
        evaluate(context).map(KnolusString::string)

    override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> =
        evaluate(context).flatMap { knolusString ->
            val str = knolusString.string
            if (str.contains('.'))
                KnolusResult.success(str.toDouble())
            else
                KnolusResult.success(str.toIntBaseN())
        }

    override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> =
        evaluate(context).flatMap { knolusString -> KnolusResult.success(knolusString.string.toFormattedBoolean()) }

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