package com.newhome.dto

import android.graphics.drawable.Drawable
import java.io.Serializable

class Solicitacao : Serializable {
    var id: SolicitacaoID? = null
    lateinit var imagemSolicitadorURL: String
    lateinit var titulo: String
    lateinit var descricao: String
}