package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.Usuario
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.services.IUsuarioService
import kotlinx.coroutines.*

class UsuarioService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IUsuarioService {
    private lateinit var usuario: Usuario

    override fun getUsuarioAtual(): Usuario = usuario.copy()

    override suspend fun getImagemUsuario(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.IO).async {
            return@async usuarioProvider.getImagemUsuario(id).await()
        }

    override suspend fun getUsuarioSemImagem(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.IO).async {
            val usuario = usuarioProvider.getUsuario(id).await()
            return@async UsuarioAsync(usuario.id, usuario.nome, usuario.detalhes)
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.IO).async {
            val imageTask = usuarioProvider.getImagemUsuario(id)
            val usuario = usuarioProvider.getUsuario(id).await()
            return@async UsuarioAsync(usuario.id, usuario.nome, usuario.detalhes, imageTask)
        }

    override suspend fun carregarUsuarioAtual(): Deferred<Usuario> =
        CoroutineScope(Dispatchers.IO).async {
            val uid = contaProvider.getContaID()!!

            val imageTask = usuarioProvider.getImagemUsuario(uid)
            val u = usuarioProvider.getUsuario(uid).await()

            usuario = Usuario.fromData(u)
            usuario.imagem = imageTask.await()

            return@async usuario
        }

    override suspend fun editarUsuarioAtual(usuario: Usuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            if (usuario.id != this@UsuarioService.usuario.id) {
                throw Exception("Não pode editar outro perfil, apenas o seu próprio perfil")
            }
            usuarioProvider.editarUsuario(usuario).await()
        }
}
