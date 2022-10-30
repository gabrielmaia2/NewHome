package com.newhome.dto

import java.io.Serializable

data class SolicitacaoPreviewData(
    var id: SolicitacaoID? = null,
    var titulo: String = "",
    var descricao: String = ""
) : Serializable
