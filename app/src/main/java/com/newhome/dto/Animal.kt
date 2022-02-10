package com.newhome.dto

import android.graphics.Bitmap
import java.io.Serializable

class Animal {
    var id: String = ""
    lateinit var nome: String
    lateinit var detalhes: String
    var imagem: Bitmap? = null
    // TODO location on maps

    // para poder pesquisar animal na lista de animais
    override fun toString(): String {
        return nome.lowercase() + detalhes.lowercase()
    }
}
