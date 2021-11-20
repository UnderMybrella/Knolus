package dev.brella.knolus.types

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.map

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

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun evaluate(context: KnolusContext): KorneaResult<KnolusString> =
        evaluateLazyString(components, context)
}

suspend fun evaluateLazyString(components: Array<KnolusUnion.StringComponent>, context: KnolusContext): KorneaResult<KnolusString> =
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