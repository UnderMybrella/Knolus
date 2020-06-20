package org.abimon.knolus.context

import org.abimon.knolus.KnolusUnion
import org.abimon.knolus.restrictions.KnolusRestriction

class KnolusGlobalContext<R>(parent: KnolusContext<R>?, restrictions: KnolusRestriction<R>): KnolusContext<R>(parent, restrictions)