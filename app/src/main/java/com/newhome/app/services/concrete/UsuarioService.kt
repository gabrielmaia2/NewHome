package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.User
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.dto.UserData
import com.newhome.app.services.IUsuarioService
import kotlinx.coroutines.*

class UsuarioService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider
) : IUsuarioService {
    private lateinit var user: User

    override fun getUsuarioAtual(): User {
        try {
            return user.copy()
        } catch (e: UninitializedPropertyAccessException) {
            throw Exception("User not signed in.")
        }
    }

    override suspend fun getImagemUsuario(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async usuarioProvider.getUserImage(id).await()
        }

    override suspend fun getUsuarioSemImagem(id: String): Deferred<UserData> =
        CoroutineScope(Dispatchers.Main).async {
            return@async usuarioProvider.getUser(id).await()
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = usuarioProvider.getUserImage(id)
            val usuario = usuarioProvider.getUser(id).await()
            return@async UsuarioAsync(usuario.id, usuario.name, usuario.details, imageTask)
        }

    override suspend fun carregarUsuarioAtual(): Deferred<User> =
        CoroutineScope(Dispatchers.Main).async {
            val uid = contaProvider.getContaID() ?: throw Exception("User not signed in.")

            val u = getUsuario(uid).await()
            user = User.fromData(u)

            return@async user.copy()
        }

    override suspend fun editarUsuarioAtual(user: User): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            if (user.id != this@UsuarioService.user.id) {
                throw Exception("A user can only edit its own profile.")
            }
            val updateUserTask =
                usuarioProvider.updateUser(UserData(user.id, user.name, user.details))
            val updateImageTask = usuarioProvider.setUserImage(user.id, user.image)

            updateUserTask.await()
            updateImageTask.await()
        }
}
