package com.newhome.dto

import android.graphics.Bitmap

data class Usuario(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = "",
    var imagem: Bitmap? = null
) {
    companion object {
        fun fromData(data: UsuarioData): Usuario {
            return Usuario(data.id, data.nome, data.detalhes)
        }

        suspend fun fromData(data: UsuarioAsync): Usuario {
            return Usuario(data.id, data.nome, data.detalhes, data.getImagem!!.await())
        }
    }
}
