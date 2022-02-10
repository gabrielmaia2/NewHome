package com.newhome.dto

import java.io.Serializable

class Solicitacao : Serializable {
    var id: SolicitacaoID? = null
    lateinit var animal: Animal
    lateinit var solicitador: Usuario
}
