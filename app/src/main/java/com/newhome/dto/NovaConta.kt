package com.newhome.dto

import java.io.Serializable

class NovaConta : Serializable {
    lateinit var email: String
    lateinit var senha: String
    var idade: Int = 0
}
