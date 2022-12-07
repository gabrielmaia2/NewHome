package com.newhome.app.dto

import java.io.Serializable

data class UpdateAnimalData(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var beingAdopted: Boolean = false
) : Serializable
