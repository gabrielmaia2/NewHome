package com.newhome.dto

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

class AnimalAsync(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = "",
    var getImagem: Deferred<Bitmap>? = null
) {
    // TODO location on maps

    // para poder pesquisar animal na lista de animais
    override fun toString(): String {
        return nome.lowercase() + detalhes.lowercase()
    }

    companion object {
        fun fromData(data: AnimalData): AnimalAsync {
            return AnimalAsync(data.id, data.nome, data.detalhes)
        }
    }
}
