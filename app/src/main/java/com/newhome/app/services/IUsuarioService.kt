package com.newhome.app.services

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred
import com.newhome.app.dto.User
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.dto.UserData

interface IUsuarioService {
    fun getUsuarioAtual(): User

    suspend fun getImagemUsuario(id: String): Deferred<Bitmap>

    suspend fun getUsuarioSemImagem(id: String): Deferred<UserData>

    suspend fun getUsuario(id: String): Deferred<UsuarioAsync>

    suspend fun carregarUsuarioAtual(): Deferred<User>

    suspend fun editarUsuarioAtual(user: User): Deferred<Unit>
}
