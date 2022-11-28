package com.newhome.app.dto

import java.io.Serializable

data class UserData(
    var id: String = "",
    var name: String = "",
    var details: String = ""
) : Serializable {
    companion object {
        val empty = UserData("0", "null", "null")
    }
}
