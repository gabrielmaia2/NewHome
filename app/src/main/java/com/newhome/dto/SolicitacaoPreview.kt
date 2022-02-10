package com.newhome.dto

import android.graphics.Bitmap
import java.io.Serializable

class SolicitacaoPreview {
    var id: SolicitacaoID? = null
    var imagemSolicitador: Bitmap? = null
    lateinit var titulo: String
    lateinit var descricao: String
}
