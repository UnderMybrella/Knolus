package org.abimon.knolus

import org.abimon.kornea.annotations.AvailableSince

@AvailableSince(Knolus.VERSION_1_2_0)
object Knolus {
    /**
     * Adds [KnolusVisitor.visitStringValue] and [KnolusVisitor.visitPlainString]
     * Adds [org.abimon.knolus.transform.TransKnolusVisitor.visitStringValue] and [org.abimon.knolus.transform.TransKnolusVisitor.visitPlainString]
     * Adds [org.abimon.knolus.transform.StringValueBlueprint] and [org.abimon.knolus.transform.PlainStringBlueprint]
     */
    @AvailableSince(VERSION_1_3_0)
    const val VERSION_1_3_0 = "1.3.0"

    @AvailableSince(VERSION_1_2_0)
    const val VERSION_1_2_0 = "1.2.0"
}