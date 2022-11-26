package com.newhome.app.services

import android.graphics.Bitmap
import com.google.firebase.firestore.*
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Credenciais
import com.newhome.app.dto.NovaConta
import com.newhome.app.services.concrete.ContaService
import org.junit.Assert.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContaServiceTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var usuarioProvider: IUsuarioProvider
    private lateinit var contaProvider: IContaProvider

    private lateinit var service: ContaService

    @Before
    fun setup() {
        usuarioProvider = MockUtils.mockUsuarioProvider()
        contaProvider = MockUtils.mockContaProvider()

        service = ContaService(usuarioProvider, contaProvider)
    }

    @Test
    fun `verify get account id`() = runTest {
        val contaId = service.getContaID()
        assertEquals("currentuserid", contaId)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify send email verification`() = runTest {
        service.enviarEmailConfirmacao().await()
        coVerify(exactly = 1) { contaProvider.enviarEmailConfirmacao() }
    }

    @Test
    fun `verify sign up invalid name`() = runTest {
        val novaConta = NovaConta("emailcorreto@example.com", "#SenhaCorreta", "Nom", 18)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(novaConta).await() }
        assertEquals("Nome deve ter entre 4 e 128 caracteres.", e.message)

        // length is 129
        novaConta.nome = "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande " +
                "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande N"
        e = TestUtils.assertThrowsAsync { service.cadastrar(novaConta).await() }
        assertEquals("Nome deve ter entre 4 e 128 caracteres.", e.message)
    }

    @Test
    fun `verify sign up invalid age`() = runTest {
        val novaConta = NovaConta("emailcorreto@example.com", "#SenhaCorreta", "Nome Correto", 17)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(novaConta).await() }
        assertEquals("Idade deve estar entre 18 e 80.", e.message)

        novaConta.idade = 81
        e = TestUtils.assertThrowsAsync { service.cadastrar(novaConta).await() }
        assertEquals("Idade deve estar entre 18 e 80.", e.message)
    }

    @Test
    fun `verify sign up invalid password`() = runTest {
        val novaConta = NovaConta("emailcorreto@example.com", "#Senha1", "Nome Correto", 18)
        var e = TestUtils.assertThrowsAsync<Exception> { service.cadastrar(novaConta).await() }
        assertEquals("Senha deve ter entre 8 e 64 caracteres.", e.message)

        // length is 65
        novaConta.senha = "#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra123"
        e = TestUtils.assertThrowsAsync { service.cadastrar(novaConta).await() }
        assertEquals("Senha deve ter entre 8 e 64 caracteres.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign up valid data`() = runTest {
        val novaConta = NovaConta("emailcorreto@example.com", "#SenhaCorreta123", "Nome", 18)
        service.cadastrar(novaConta).await()

        // length is 128
        novaConta.nome = "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande " +
                "Nomemuitogrande Nomemuitogrande Nomemuitogrande Nomemuitogrande "
        service.cadastrar(novaConta).await()

        novaConta.idade = 18
        service.cadastrar(novaConta).await()

        novaConta.idade = 80
        service.cadastrar(novaConta).await()

        novaConta.senha = "#Senha12"
        service.cadastrar(novaConta).await()

        // length is 64
        novaConta.senha = "#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12#SenhaMuitoGra12"
        service.cadastrar(novaConta).await()

        coVerify(exactly = 6) { contaProvider.criarConta(any()) }
        coVerify(exactly = 6) { contaProvider.enviarEmailConfirmacao() }
        coVerify(exactly = 6) { usuarioProvider.createUser(any()) }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in no email verified`() = runTest {
        val contaProvider = MockUtils.mockContaProvider()
        val service = ContaService(usuarioProvider, contaProvider)
        every { contaProvider.emailConfirmacaoVerificado() } returns false

        val e = TestUtils.assertThrowsAsync<Exception> {
            service.logar(Credenciais("emailcorreto@example.com", "#SenhaCorreta123")).await()
        }
        coVerify(exactly = 1) { contaProvider.logar(any()) }
        coVerify(exactly = 1) { contaProvider.enviarEmailConfirmacao() }
        coVerify(exactly = 1) { contaProvider.sair() }
        assertEquals(
            "Email not verified. Please, verify your email address before signing in.",
            e.message
        )
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in`() = runTest {
        service.logar(Credenciais("emailcorreto@example.com", "#SenhaCorreta123")).await()
        coVerify(exactly = 1) { contaProvider.logar(any()) }
        coVerify(exactly = 0) { contaProvider.enviarEmailConfirmacao() }
        coVerify(exactly = 0) { contaProvider.sair() }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in with google first time`() = runTest {
//        val usuarioProvider = MockUtils.mockUsuarioProvider()
//        val service = ContaService(usuarioProvider, contaProvider)
//
//        val account = mockk<GoogleSignInAccount>()
//        val photoUrl = mockk<Uri>()
//        coEvery { account.displayName } returns "Nome Correto"
//        coEvery { account.photoUrl } returns photoUrl
//        coEvery { photoUrl.toString() } returns "https://www.example.com"
//
//        mockkStatic(Utils::class)
//        every { Utils.uriToBitmap(photoUrl) } returns defaultBitmap
//
//        val exceptionTask = CoroutineScope(Dispatchers.Main).async {
//            throw NoSuchElementException("Couldn't find user with specified ID.")
//        }
//        coEvery { usuarioProvider.getUser(any()) } returns exceptionTask
//
//        service.entrarComGoogle(account).await()
//        coVerify(exactly = 1) { contaProvider.entrarComGoogle(any()) }
//        coVerify(exactly = 1) { usuarioProvider.createUser(any()) }
//        coVerify(exactly = 1) { usuarioProvider.setUserImage(any(), defaultBitmap) }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign in with google`() = runTest {
        service.entrarComGoogle(mockk()).await()
        coVerify(exactly = 1) { contaProvider.entrarComGoogle(any()) }
        coVerify(exactly = 0) { usuarioProvider.createUser(any()) }
        coVerify(exactly = 0) { usuarioProvider.setUserImage(any(), any()) }
    }

    @Test
    fun `verify try use signed in account not signed in`() = runTest {
        val contaProvider = MockUtils.mockContaProvider()
        val service = ContaService(usuarioProvider, contaProvider)
        every { contaProvider.getContaID() } returns null

        val e = TestUtils.assertThrowsAsync<Exception> { service.tentarUsarContaLogada() }
        assertEquals("User not signed in.", e.message)
    }

    @Test
    fun `verify try use signed in account`() = runTest {
        service.tentarUsarContaLogada()
        assertEquals("currentuserid", service.getContaID())
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify sign out`() = runTest {
        service.sair().await()
        coVerify(exactly = 1) { contaProvider.sair() }
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify delete account`() = runTest {
        // TODO
        service.excluirConta().await()
        coVerify(exactly = 1) { contaProvider.excluirConta() }
    }
}
