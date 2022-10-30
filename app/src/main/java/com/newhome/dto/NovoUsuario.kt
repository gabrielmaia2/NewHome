package com.newhome.dto

import java.io.Serializable

data class NovoUsuario(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = "",
    var idade: Int = 0
) : Serializable
