package dev.brella.knolus.context

import dev.brella.knolus.KnolusFunction
import dev.brella.knolus.restrictions.KnolusRestriction

@ExperimentalUnsignedTypes
class KnolusFunctionContext<T>(
    val function: KnolusFunction<T>,
    parent: KnolusContext?,
    restrictions: KnolusRestriction<*>,
) : KnolusContext(parent, restrictions) {
    override fun recursiveCountFor(func: KnolusFunction<*>): Int =
        if (func === function) 1 + super.recursiveCountFor(func) else super.recursiveCountFor(func)
}