package dev.brella.knolus.context

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.restrictions.KnolusRestriction

class KnolusGlobalContext(parent: KnolusContext?, restrictions: KnolusRestriction<*>): KnolusContext(parent, restrictions)