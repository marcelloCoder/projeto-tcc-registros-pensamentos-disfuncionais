package br.com.mcoder.primeiroprojeto

import br.com.mcoder.primeiroprojeto.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun hash_isStableForSameInput() {
        val first = PasswordHasher.hash("secret123")
        val second = PasswordHasher.hash("secret123")

        assertEquals(first, second)
    }

    @Test
    fun hash_changesForDifferentInput() {
        val first = PasswordHasher.hash("secret123")
        val second = PasswordHasher.hash("secret456")

        assertNotEquals(first, second)
    }
}
