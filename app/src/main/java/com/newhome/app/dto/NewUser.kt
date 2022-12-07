package com.newhome.app.dto

import java.io.Serializable

data class NewUser(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var age: Int = 0
) : Serializable
