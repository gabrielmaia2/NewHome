package com.newhome.dto

import android.graphics.drawable.Drawable
import java.io.Serializable

class Animal : Serializable {
    var id: String = ""
    lateinit var nome: String
    lateinit var detalhes: String
    lateinit var imagemURL: String
    // TODO location on maps
}
