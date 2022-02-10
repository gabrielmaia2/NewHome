package com.newhome.dto

import android.graphics.Bitmap
import java.io.Serializable

class Usuario {
    var id: String = ""
    lateinit var nome: String
    lateinit var detalhes: String
    var imagem: Bitmap? = null
}
