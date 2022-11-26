package com.newhome.app.dao

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.firebase.FirebaseContaProvider
import com.newhome.app.dto.Credenciais
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.BeforeClass
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseContaProviderTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var context: Context
    private lateinit var auth: FirebaseAuth
    private lateinit var authUI: AuthUI

    private lateinit var provider: FirebaseContaProvider
    private lateinit var providerBeforeSignIn: FirebaseContaProvider

    @Before
    fun setup() {
        context = mockk()
        auth = MockUtils.mockFirebaseAuth()
        authUI = MockUtils.mockAuthUI()
        provider = FirebaseContaProvider(auth, authUI, context)

        val authBeforeSignIn = MockUtils.mockFirebaseAuth()
        providerBeforeSignIn = FirebaseContaProvider(authBeforeSignIn, authUI, context)
        coEvery { authBeforeSignIn.currentUser } returns null
    }

    @Test
    fun `verify get account id before sign in`() = runTest {
        val provider = providerBeforeSignIn
        val contaId = provider.getContaID()
        assertEquals(null, contaId)
    }

    @Test
    fun `verify get account id`() = runTest {
        val contaId = provider.getContaID()
        assertEquals("currentuserid", contaId)
    }

    @Test
    fun `verify send email verification before sign in`() = runTest {
        val provider = providerBeforeSignIn
        val e = TestUtils.assertThrowsAsync<Exception> { provider.enviarEmailConfirmacao().await() }
        assertEquals("User not signed in.", e.message)
    }

    @Test
    fun `verify send email verification`() = runTest {
        provider.enviarEmailConfirmacao().await()
        val currentUser = auth.currentUser!!
        coVerify(exactly = 1) { currentUser.sendEmailVerification() }
    }

    @Test
    fun `verify confirmation email verified before sign in`() = runTest {
        val provider = providerBeforeSignIn
        val e = TestUtils.assertThrowsAsync<Exception> { provider.emailConfirmacaoVerificado() }
        assertEquals("User not signed in.", e.message)
    }

    @Test
    fun `verify create account`() = runTest {
        provider.criarConta(Credenciais("email@example.com", "#Senha123")).await()
        coVerify(exactly = 1) { auth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `verify sign in wrong email and password`() = runTest {
        val credenciais = Credenciais("emailerrado@example.com", "#Senhaerrada123")
        TestUtils.assertThrowsAsync<Exception> { provider.logar(credenciais).await() }
    }

    @Test
    fun `verify sign in wrong email`() = runTest {
        val credenciais = Credenciais("emailerrado@example.com", "#SenhaCorreta123")
        TestUtils.assertThrowsAsync<Exception> { provider.logar(credenciais).await() }
    }

    @Test
    fun `verify sign in wrong password`() = runTest {
        val credenciais = Credenciais("emailcorreto@example.com", "#Senhaerrada123")
        TestUtils.assertThrowsAsync<Exception> { provider.logar(credenciais).await() }
    }

    @Test
    fun `verify sign in`() = runTest {
        val credenciais = Credenciais("emailcorreto@example.com", "#SenhaCorreta123")
        provider.logar(credenciais).await()
        coVerify(exactly = 1) {
            auth.signInWithEmailAndPassword(
                "emailcorreto@example.com",
                "#SenhaCorreta123"
            )
        }
    }

    @Test
    fun `verify google sign in`() = runTest {
        provider.entrarComGoogle(mock()).await()
        coVerify(exactly = 1) { auth.signInWithCredential(any()) }
    }

    @Test
    fun `verify sign out`() = runTest {
        provider.sair().await()
        coVerify(exactly = 1) { authUI.signOut(any()) }
    }

    @Test
    fun `verify delete account`() = runTest {
        provider.excluirConta().await()
        coVerify(exactly = 1) { authUI.delete(any()) }
    }
}
