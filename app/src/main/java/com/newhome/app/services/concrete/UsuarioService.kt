package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.User
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.dto.UserData
import com.newhome.app.services.IUsuarioService
import kotlinx.coroutines.*

class UsuarioService(
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider,
    private val imageProvider: IImageProvider
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
        imageProvider.getUserImage(id)

    override suspend fun getUsuarioSemImagem(id: String): Deferred<UserData> =
        CoroutineScope(Dispatchers.Main).async {
            val task = usuarioProvider.runTransaction { t -> usuarioProvider.getUser(t, id) }
            return@async task.await() ?: UserData.empty
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = getImagemUsuario(id)
            val task = usuarioProvider.runTransaction { t -> usuarioProvider.getUser(t, id) }
            val usuario = task.await() ?: UserData.empty
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
            val u = UserData(user.id, user.name, user.details)
            val updateUserTask = usuarioProvider.runTransaction { t ->
                usuarioProvider.updateUser(t, u)
            }
            val updateImageTask = imageProvider.saveUserImage(user.id, user.image)

            updateUserTask.await()
            updateImageTask.await()
        }
}
