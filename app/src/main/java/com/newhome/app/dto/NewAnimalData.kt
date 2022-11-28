package com.newhome.app.dto

import java.io.Serializable

data class NewAnimalData(
    var name: String = "",
    var details: String = "",
    var donorId: String = ""
) : Serializable
