package com.newhome.dao

import android.graphics.Bitmap
import com.newhome.dto.NovoUsuario
import kotlinx.coroutines.Deferred
import com.newhome.dto.Usuario
import com.newhome.dto.UsuarioData

interface IUsuarioProvider {
    suspend fun getImagemUsuario(id: String): Deferred<Bitmap>

    suspend fun getUsuario(id: String): Deferred<UsuarioData>

    suspend fun criarUsuario(usuario: NovoUsuario): Deferred<Unit>

    suspend fun editarUsuario(usuario: Usuario): Deferred<Unit>

    suspend fun deleteUsuario(id: String): Deferred<Unit>
}
