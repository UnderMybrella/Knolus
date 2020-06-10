package org.abimon.knolus.context

import org.abimon.knolus.KnolusFunction
import org.abimon.knolus.restrictions.KnolusRestrictions

class KnolusFunctionContext<T>(
    val function: KnolusFunction<T>,
    parent: KnolusContext?,
    restrictions: KnolusRestrictions,
) : KnolusContext(parent, restrictions) {
    override fun <T> countFunctionRecursion(func: KnolusFunction<T>): Int =
        if (func === function) 1 + super.countFunctionRecursion(func) else super.countFunctionRecursion(func)
}