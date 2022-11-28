package com.newhome.app.dto

import java.io.Serializable

data class NewAccount(
    var email: String = "",
    var password: String = "",
    var name: String = "",
    var age: Int = 0
) : Serializable
