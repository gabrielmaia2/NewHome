package com.newhome.app.dto

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

class AnimalAsync(
    var id: String = "",
    var name: String = "",
    var details: String = "",
    var getImage: Deferred<Bitmap>? = null
) {
    // TODO location on maps

    // para poder pesquisar animal na lista de animais
    override fun toString(): String {
        return name.lowercase() + details.lowercase()
    }

    companion object {
        fun fromData(data: AnimalData): AnimalAsync {
            return AnimalAsync(data.id, data.name, data.details)
        }

        val empty = AnimalAsync("0", "null", "null")
    }
}
