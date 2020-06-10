package org.abimon.knolus.context

import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.restrictions.KnolusRestrictions

class KnolusScopeContext(val scope: KnolusUnion.ScopeType, parent: KnolusContext?, restrictions: KnolusRestrictions): KnolusContext(parent, restrictions)