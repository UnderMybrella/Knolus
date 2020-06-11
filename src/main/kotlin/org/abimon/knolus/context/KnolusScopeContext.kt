package org.abimon.knolus.context

import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.restrictions.KnolusRestriction

class KnolusScopeContext<R>(val scope: KnolusUnion.ScopeType, parent: KnolusContext<R>?, restrictions: KnolusRestriction<R>): KnolusContext<R>(parent, restrictions)