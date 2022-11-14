package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IImageProvider
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

    override fun getUsuarioAtual(): Usuario = usuario.copy()

    override suspend fun getImagemUsuario(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async usuarioProvider.getUserImage(id).await()
        }

    override suspend fun getUsuarioSemImagem(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val usuario = usuarioProvider.getUser(id).await()
            return@async UsuarioAsync(usuario.id, usuario.nome, usuario.detalhes)
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = usuarioProvider.getUserImage(id)
            val usuario = usuarioProvider.getUser(id).await()
            return@async UsuarioAsync(usuario.id, usuario.nome, usuario.detalhes, imageTask)
        }

    override suspend fun carregarUsuarioAtual(): Deferred<Usuario> =
        CoroutineScope(Dispatchers.Main).async {
            val uid = contaProvider.getContaID()!!

            val imageTask = usuarioProvider.getUserImage(uid)
            val u = usuarioProvider.getUser(uid).await()

            usuario = Usuario.fromData(u)
            usuario.imagem = imageTask.await()

            return@async usuario
        }

    override suspend fun editarUsuarioAtual(usuario: Usuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            if (usuario.id != this@UsuarioService.usuario.id) {
                throw Exception("Não pode editar outro perfil, apenas o seu próprio perfil")
            }
            val updateUserTask =
                usuarioProvider.updateUser(UsuarioData(usuario.id, usuario.nome, usuario.detalhes))
            val updateImageTask = usuarioProvider.setUserImage(usuario.id, usuario.imagem)

            updateUserTask.await()
            updateImageTask.await()
        }
}
