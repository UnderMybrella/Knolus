package dev.brella.knolus

import dev.brella.kornea.annotations.AvailableSince

@AvailableSince(Knolus.VERSION_1_2_0)
object Knolus {
    const val VERSION_1_4_0 = "1.4.0"

    /**
     * Adds [KnolusVisitor.visitStringValue] and [KnolusVisitor.visitPlainString]
     * Adds [dev.brella.knolus.transform.TransKnolusVisitor.visitStringValue] and [dev.brella.knolus.transform.TransKnolusVisitor.visitPlainString]
     * Adds [dev.brella.knolus.transform.StringValueBlueprint] and [dev.brella.knolus.transform.PlainStringBlueprint]
     */
    @AvailableSince(VERSION_1_3_0)
    const val VERSION_1_3_0 = "1.3.0"

    @AvailableSince(VERSION_1_2_0)
    const val VERSION_1_2_0 = "1.2.0"
}