package com.newhome.app.dto

import java.io.Serializable

data class UsuarioData(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = ""
) : Serializable {
    companion object {
        val empty = UsuarioData("0", "null", "null")
    }
}
