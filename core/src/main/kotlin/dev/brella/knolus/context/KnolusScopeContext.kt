package dev.brella.knolus.context

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.restrictions.KnolusRestriction

class KnolusScopeContext<R>(val scope: KnolusUnion.ScopeType, parent: KnolusContext<R>?, restrictions: KnolusRestriction<R>): KnolusContext<R>(parent, restrictions)