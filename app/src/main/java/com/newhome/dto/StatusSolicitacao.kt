package com.newhome.dto

import java.io.Serializable

class StatusSolicitacao : Serializable {
    var solicitado: Boolean = false
    var solicitacaoAceita: Boolean = false
    var detalhesAdocao: String = ""
}
