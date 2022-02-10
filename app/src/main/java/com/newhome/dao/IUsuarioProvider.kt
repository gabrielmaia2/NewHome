package com.newhome.dao

import android.content.Context
import com.newhome.dto.Usuario

interface IUsuarioProvider {
    fun getUsuarioAtual(
        onSuccess: (usuario: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getUsuario(
        id: String,
        onSuccess: (usuario: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun editarUsuarioAtual(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
}
