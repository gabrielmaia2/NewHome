package com.newhome.app.dto

import android.graphics.Bitmap

data class Animal(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = "",
    var imagem: Bitmap? = null
) {
    // TODO location on maps

    // para poder pesquisar animal na lista de animais
    override fun toString(): String {
        return nome.lowercase() + detalhes.lowercase()
    }

    companion object {
        fun fromData(data: AnimalData): Animal {
            return Animal(data.id, data.nome, data.detalhes)
        }

        suspend fun fromData(data: AnimalAsync): Animal {
            return Animal(data.id, data.nome, data.detalhes, data.getImagem!!.await())
        }
    }
}
