package com.newhome.app

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.firebase.FirebaseContaProvider
import com.newhome.app.dto.Credenciais
import com.newhome.app.utils.Utils
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class UtilsTest {
    private lateinit var context: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var authUI: AuthUI

    private lateinit var provider: FirebaseContaProvider
    private lateinit var providerBeforeSignIn: FirebaseContaProvider

    @Before
    fun setup() {
    }

    @Test
    fun `verify sha256`() = runTest {
        val data = "sha256 test"
        val hash = Utils.sha256(data.toByteArray())
        val expectedHash = "9c4911dea2eadbf8e31b028b2130d80cbfdbdc2e0acd083d5a60ad641298a2bd"
        assertEquals(expectedHash, hash)
    }

    @Test
    fun `verify sha256 with zero padding`() = runTest {
        val data = "8uwijsfagwfskdjfkgoskfldlakgjemfoskdogjskzoakdieofkgnslzodkfiangoekdlfogn"
        val hash = Utils.sha256(data.toByteArray())
        val expectedHash = "002fad05da0e93d0112c3fbaeabb31621a5fb27c11eefd21b17c2c88f21df9ef"
        assertEquals(expectedHash, hash)
    }
}
