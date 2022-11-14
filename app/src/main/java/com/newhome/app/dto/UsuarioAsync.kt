package com.newhome.app.dto

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

class UsuarioAsync(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = "",
    var getImagem: Deferred<Bitmap>? = null
) {
    companion object {
        fun fromData(data: UsuarioData): UsuarioAsync {
            return UsuarioAsync(data.id, data.nome, data.detalhes)
        }
    }
}
