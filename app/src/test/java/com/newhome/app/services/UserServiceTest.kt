package com.newhome.app.services

import android.graphics.Bitmap
import com.google.firebase.firestore.*
import com.newhome.app.MockUtils
import com.newhome.app.TestUtils
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.User
import com.newhome.app.services.concrete.UsuarioService
import org.junit.Assert.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class UserServiceTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            MockUtils.init()
        }
    }

    private lateinit var defaultBitmap: Bitmap
    private lateinit var nonDefaultBitmap: Bitmap

    private lateinit var usuarioProvider: IUsuarioProvider
    private lateinit var contaProvider: IContaProvider
    private lateinit var imageProvider: IImageProvider

    private lateinit var service: UsuarioService

    @Before
    fun setup() {
        defaultBitmap = MockUtils.defaultBitmap
        nonDefaultBitmap = MockUtils.nonDefaultBitmap

        contaProvider = MockUtils.mockContaProvider()
        usuarioProvider = MockUtils.mockUsuarioProvider()
        imageProvider = MockUtils.mockImageProvider("usuarios/currentuserid", "usuarios/userid")

        service = UsuarioService(usuarioProvider, contaProvider, imageProvider)
    }

    @Test
    fun `verify get current user before sign in`() = runTest {
        val contaProvider = MockUtils.mockContaProvider()
        val service = UsuarioService(usuarioProvider, contaProvider, imageProvider)

        coEvery { contaProvider.getContaID() } returns null

        val e = TestUtils.assertThrowsAsync<Exception> { service.getUsuarioAtual() }
        assertEquals("User not signed in.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify load and get current user`() = runTest {
        val usuario = service.carregarUsuarioAtual().await()
        val usuario2 = service.getUsuarioAtual()
        coVerify(exactly = 1) { contaProvider.getContaID() }
        coVerify(exactly = 1) { usuarioProvider.getUser(any(), "currentuserid") }
        coVerify(exactly = 1) { imageProvider.getUserImage("currentuserid") }

        // getUsuarioAtual() should always return a copy of the original object
        assertEquals("currentuserid", usuario.id)
        assertEquals(usuario, usuario2)
        assertNotSame(usuario, usuario2)

        // carregarUsuarioAtual() should always return a copy of the original object
        usuario.id = "changedid"
        val usuario3 = service.getUsuarioAtual()
        assertEquals("currentuserid", usuario3.id)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify get user image`() = runTest {
        val imagem = service.getImagemUsuario("userid").await()
        coVerify(exactly = 1) { imageProvider.getUserImage("userid") }
        assertEquals(nonDefaultBitmap, imagem)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify get user without image`() = runTest {
        val usuario = service.getUsuarioSemImagem("userid").await()
        coVerify(exactly = 1) { usuarioProvider.getUser(any(), "userid") }
        assertEquals("userid", usuario.id)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify get user`() = runTest {
        val usuario = service.getUsuario("userid").await()
        coVerify(exactly = 1) { usuarioProvider.getUser(any(), "userid") }
        coVerify(exactly = 1) { imageProvider.getUserImage("userid") }
        assertEquals("userid", usuario.id)
    }

    @Test
    fun `verify update current user wrong id`() = runTest {
        service.carregarUsuarioAtual().await()
        val e = TestUtils.assertThrowsAsync<Exception> {
            service.editarUsuarioAtual(User("userid", "nome2", "detalhes2", nonDefaultBitmap))
                .await()
        }
        assertEquals("A user can only edit its own profile.", e.message)
    }

    @Test
    @Suppress("DeferredResultUnused")
    fun `verify update current user`() = runTest {
        service.carregarUsuarioAtual().await()
        service.editarUsuarioAtual(
            User(
                "currentuserid",
                "nome2",
                "detalhes2",
                nonDefaultBitmap
            )
        ).await()
        verify(exactly = 1) { usuarioProvider.updateUser(any(), any()) }
        coVerify(exactly = 1) { imageProvider.saveUserImage("currentuserid", nonDefaultBitmap) }
    }
}