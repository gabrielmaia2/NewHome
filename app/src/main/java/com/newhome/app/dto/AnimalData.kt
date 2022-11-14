package com.newhome.app.dto

import java.io.Serializable

data class AnimalData(
    var id: String = "",
    var nome: String = "",
    var detalhes: String = ""
) : Serializable
