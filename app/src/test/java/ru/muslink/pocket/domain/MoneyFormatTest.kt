package ru.muslink.pocket.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatTest {

    @Test
    fun formatKopecks_usesRublesAndKopecks() {
        assertEquals("0,00 ₽", MoneyFormat.formatKopecks(0))
        assertEquals("1,50 ₽", MoneyFormat.formatKopecks(150))
        assertEquals("1 500,00 ₽", MoneyFormat.formatKopecks(150_000))
        assertEquals("1 234 567,89 ₽", MoneyFormat.formatKopecks(123_456_789))
        assertEquals("-250,00 ₽", MoneyFormat.formatKopecks(-25_000))
    }

    @Test
    fun formatAmount_hasNoCurrencySuffix() {
        assertEquals("1 500,00", MoneyFormat.formatAmount(150_000))
    }

    @Test
    fun parseToKopecks_acceptsCommaAndDot() {
        assertEquals(150_000L, RubleParser.parseToKopecks("1500"))
        assertEquals(150_050L, RubleParser.parseToKopecks("1500,50"))
        assertEquals(150_050L, RubleParser.parseToKopecks("1500.5"))
        assertEquals(150_000L, RubleParser.parseToKopecks(" 1 500 "))
        assertEquals(123L, RubleParser.parseToKopecks("1,234"))
    }

    @Test
    fun parseToKopecks_rejectsGarbage() {
        assertNull(RubleParser.parseToKopecks(""))
        assertNull(RubleParser.parseToKopecks("abc"))
        assertNull(RubleParser.parseToKopecks("-100"))
    }

    @Test
    fun toInputString_roundTrips() {
        assertEquals("1500", RubleParser.toInputString(150_000))
        assertEquals("1500.5", RubleParser.toInputString(150_050))
    }
}
