package com.newhome.dto

import java.io.Serializable

class NovaConta : Serializable {
    lateinit var email: String
    lateinit var senha: String
    lateinit var nome: String
    var idade: Int = 0
}
