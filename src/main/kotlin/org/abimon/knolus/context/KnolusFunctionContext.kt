package org.abimon.knolus.context

import org.abimon.knolus.KnolusFunction
import org.abimon.knolus.restrictions.KnolusRestriction

@ExperimentalUnsignedTypes
class KnolusFunctionContext<T, R, C: KnolusContext<out R>>(
    val function: KnolusFunction<T, R, C>,
    parent: KnolusContext<R>?,
    restrictions: KnolusRestriction<R>,
) : KnolusContext<R>(parent, restrictions) {
    override fun recursiveCountFor(func: KnolusFunction<*, R, *>): Int =
        if (func === function) 1 + super.recursiveCountFor(func) else super.recursiveCountFor(func)
}