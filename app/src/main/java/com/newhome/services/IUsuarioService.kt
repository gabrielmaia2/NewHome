package com.newhome.services

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred
import com.newhome.dto.Usuario
import com.newhome.dto.UsuarioAsync

interface IUsuarioService {
    fun getUsuarioAtual(): Usuario

    suspend fun getImagemUsuario(id: String): Deferred<Bitmap>

    suspend fun getUsuarioSemImagem(id: String): Deferred<UsuarioAsync>

    suspend fun getUsuario(id: String): Deferred<UsuarioAsync>

    suspend fun carregarUsuarioAtual(): Deferred<Usuario>

    suspend fun editarUsuarioAtual(usuario: Usuario): Deferred<Unit>
}
