package com.newhome.app.dto

import java.io.Serializable

data class NovaConta(
    var email: String = "",
    var senha: String = "",
    var nome: String = "",
    var idade: Int = 0
) : Serializable
