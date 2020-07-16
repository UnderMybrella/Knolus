package dev.brella.knolus.context

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.restrictions.KnolusRestriction

class KnolusGlobalContext<R>(parent: KnolusContext<R>?, restrictions: KnolusRestriction<R>): KnolusContext<R>(parent, restrictions)