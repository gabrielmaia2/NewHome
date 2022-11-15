package com.newhome.app.dto

import java.io.Serializable

data class AnimalData(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = ""
) : Serializable {
    companion object {
        val empty = AnimalData("0", "null", "null")
    }
}
