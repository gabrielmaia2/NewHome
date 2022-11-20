package com.newhome.app.services

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred
import com.newhome.app.dto.Usuario
import com.newhome.app.dto.UsuarioAsync
import com.newhome.app.dto.UsuarioData

interface IUsuarioService {
    fun getUsuarioAtual(): Usuario

    suspend fun getImagemUsuario(id: String): Deferred<Bitmap>

    suspend fun getUsuarioSemImagem(id: String): Deferred<UsuarioData>

    suspend fun getUsuario(id: String): Deferred<UsuarioAsync>

    suspend fun carregarUsuarioAtual(): Deferred<Usuario>

    suspend fun editarUsuarioAtual(usuario: Usuario): Deferred<Unit>
}
