package app.dropletmesh.id.donnees

import com.google.crypto.tink.subtle.Ed25519Verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class ProtocoleTest {

    private val graine = ByteArray(32) { it.toByte() }

    @Test
    fun texteASignerSuitLeFormatDuServeur() {
        val t = Protocole.texteASigner("POST", "/v1/comptes", 1_700_000_000, "abc", "{\"nom\":\"Pixel\"}")
        val lignes = t.split('\n')
        assertEquals(listOf("POST", "/v1/comptes", "1700000000", "abc"), lignes.take(4))
        assertEquals(64, lignes[4].length)
        // SHA-256 de la chaîne vide : valeur de référence.
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Protocole.sha256Hex(""))
    }

    @Test
    fun signatureVerifiableAvecLaClePublique() {
        val e = Protocole.entetes(graine, "GET", "/v1/connexions", "", appareilId = "app-1", clePublique = null, heure = 42, nonce = "n")
        val sig = Base64.getUrlDecoder().decode(e.getValue("x-droplet-sig"))
        val pub = Base64.getUrlDecoder().decode(Protocole.clePublique(graine))
        Ed25519Verify(pub).verify(sig, Protocole.texteASigner("GET", "/v1/connexions", 42, "n", "").toByteArray())
        assertEquals("app-1", e["x-droplet-appareil"])
        assertTrue(!e.getValue("x-droplet-sig").contains('='))
    }

    @Test
    fun liensAcceptesEtRefuses() {
        val id = "AbCdEf0123456789_-xy"
        assertEquals(LienDemande(Liens.PRODUCTION, id), Liens.demande("https://id.dropletmesh.app/a/$id"))
        assertEquals(LienDemande(Liens.TEST, id), Liens.demande(" https://id-test.dropletmesh.app/a/$id?x=1 "))
        assertNull(Liens.demande("http://id.dropletmesh.app/a/$id"))
        assertNull(Liens.demande("https://id.dropletmesh.app.evil.com/a/$id"))
        assertNull(Liens.demande("https://evil.com/a/$id"))
        assertNull(Liens.demande("https://id.dropletmesh.app/a/court"))
        assertNull(Liens.demande("https://id.dropletmesh.app/b/$id"))
    }
}
