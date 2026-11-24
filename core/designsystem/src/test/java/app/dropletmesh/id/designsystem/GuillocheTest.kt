package app.dropletmesh.id.designsystem

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GuillocheTest {

    @Test
    fun memeIdDonneMemeMotif() {
        assertEquals(ParametresGuilloche.depuis("DRP-4K7Q-92XA"), ParametresGuilloche.depuis("DRP-4K7Q-92XA"))
    }

    @Test
    fun deuxIdDonnentDeuxMotifs() {
        val ids = (0 until 50).map { "DRP-TEST-%04d".format(it) }
        val motifs = ids.map { ParametresGuilloche.depuis(it) }.toSet()
        // Des collisions exactes sur 8 paramètres tirés de SHA-256 seraient un bug.
        assertEquals(ids.size, motifs.size)
        assertNotEquals(ParametresGuilloche.depuis("a"), ParametresGuilloche.depuis("b"))
    }

    @Test
    fun parametresDansLeursBornes() {
        repeat(200) {
            val p = ParametresGuilloche.depuis("id-$it")
            assertTrue(p.lobes in 5..11)
            assertTrue(p.battement in 13..23)
            assertTrue(p.lignes in 14..22)
            assertTrue(p.centreX in 0.619f..0.841f)
            assertTrue(p.centreY in 0.349f..0.651f)
        }
    }

    @Test
    fun chaqueLigneEstFermee() {
        val p = ParametresGuilloche.depuis("DRP-4K7Q-92XA")
        repeat(p.lignes) { l ->
            val points = pointsGuilloche(p, l)
            val (x0, y0) = points.first()
            val (x1, y1) = points.last()
            assertTrue(abs(x0 - x1) < 1e-4f && abs(y0 - y1) < 1e-4f)
        }
    }
}
