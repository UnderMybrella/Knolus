package dev.brella.knolus.context

import dev.brella.knolus.KnolusUnion
import dev.brella.knolus.restrictions.KnolusRestriction

class KnolusScopeContext(val scope: KnolusUnion.ScopeType, parent: KnolusContext?, restrictions: KnolusRestriction<*>): KnolusContext(parent, restrictions)