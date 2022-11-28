package com.newhome.app.dto

import java.io.Serializable

data class AnimalData(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var donorId: String = "",
    var adopterId: String = "",
    var requestersIds: List<String> = ArrayList(),
    var beingAdopted: Boolean = false
) : Serializable {
    companion object {
        val empty = AnimalData("0", "", "")
    }
}
