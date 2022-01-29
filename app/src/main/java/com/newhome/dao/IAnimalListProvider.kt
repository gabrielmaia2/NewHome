package com.newhome.dao

import com.newhome.dto.Animal

interface IAnimalListProvider {
    fun getAnimais(pesquisa: String, onSuccess: (animais: List<Animal>) -> Unit, onFail: (e: Exception) -> Unit)
}
