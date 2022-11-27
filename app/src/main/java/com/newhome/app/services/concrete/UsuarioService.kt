package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Usuario
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.dto.UsuarioData
import com.newhome.app.services.IUsuarioService
import kotlinx.coroutines.*

class UsuarioService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IUsuarioService {
    private lateinit var usuario: Usuario

    override fun getUsuarioAtual(): Usuario {
        try {
            return usuario.copy()
        } catch (e: UninitializedPropertyAccessException) {
            throw Exception("User not signed in.")
        }
    }

    override suspend fun getImagemUsuario(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async usuarioProvider.getUserImage(id).await()
        }

    override suspend fun getUsuarioSemImagem(id: String): Deferred<UsuarioData> =
        CoroutineScope(Dispatchers.Main).async {
            return@async usuarioProvider.getUser(id).await()
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = usuarioProvider.getUserImage(id)
            val usuario = usuarioProvider.getUser(id).await()
            return@async UsuarioAsync(usuario.id, usuario.nome, usuario.detalhes, imageTask)
        }

    override suspend fun carregarUsuarioAtual(): Deferred<Usuario> =
        CoroutineScope(Dispatchers.Main).async {
            val uid = contaProvider.getContaID() ?: throw Exception("User not signed in.")

            val u = getUsuario(uid).await()
            usuario = Usuario.fromData(u)

            return@async usuario.copy()
        }

    override suspend fun editarUsuarioAtual(usuario: Usuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            if (usuario.id != this@UsuarioService.usuario.id) {
                throw Exception("A user can only edit its own profile.")
            }
            val updateUserTask =
                usuarioProvider.updateUser(UsuarioData(usuario.id, usuario.nome, usuario.detalhes))
            val updateImageTask = usuarioProvider.setUserImage(usuario.id, usuario.imagem)

            updateUserTask.await()
            updateImageTask.await()
        }
}
