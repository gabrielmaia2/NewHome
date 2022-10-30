package com.newhome.dto

import java.io.Serializable

data class UsuarioData(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = ""
) : Serializable
