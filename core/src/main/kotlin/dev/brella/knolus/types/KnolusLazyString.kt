package dev.brella.knolus.types

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.errors.common.map

inline class KnolusLazyString(val components: Array<KnolusUnion.StringComponent>) :
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
        evaluateLazyString(components, context)
}

suspend fun <T> evaluateLazyString(components: Array<KnolusUnion.StringComponent>, context: KnolusContext<T>): KorneaResult<KnolusString> =
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