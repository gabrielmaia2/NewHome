package com.newhome.dto

import android.graphics.drawable.Drawable
import android.media.Image
import java.io.Serializable

class Usuario : Serializable {
    var id: String = ""
    lateinit var nome: String
    lateinit var detalhes: String
    lateinit var imagemURL: String
}
