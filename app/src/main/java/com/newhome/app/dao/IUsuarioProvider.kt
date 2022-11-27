package com.newhome.app.dao

import android.graphics.Bitmap
import com.newhome.app.dto.NovoUsuario
import kotlinx.coroutines.Deferred
import com.newhome.app.dto.UsuarioData

interface IUsuarioProvider {
    suspend fun getUser(id: String): Deferred<UsuarioData>

    suspend fun createUser(usuario: NovoUsuario): Deferred<Unit>

    suspend fun updateUser(usuario: UsuarioData): Deferred<Unit>

    suspend fun deleteUser(id: String): Deferred<Unit>

    suspend fun getUserImage(id: String): Deferred<Bitmap>

    suspend fun setUserImage(id: String, image: Bitmap?): Deferred<Unit>
}
