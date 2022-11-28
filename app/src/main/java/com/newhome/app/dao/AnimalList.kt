package com.newhome.app.dao

enum class AnimalList {
    placedForAdoption,
    adopted,
    adoptionRequested;

    override fun toString(): String {
        return when (this) {
            placedForAdoption -> "animais"
            adopted -> "adotados"
            adoptionRequested -> "solicitados"
        }
    }
}