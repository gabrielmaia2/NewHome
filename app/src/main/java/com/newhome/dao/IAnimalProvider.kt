package com.newhome.dao

import android.content.Context
import com.newhome.dto.Animal
import com.newhome.dto.Usuario

interface IAnimalProvider {
    fun getTodosAnimais(
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getAnimaisPostosAdocao(
        donoId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getAnimaisAdotados(
        adotadorId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getAnimaisSolicitados(
        solicitadorId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getDonoInicial(
        animalId: String,
        onSuccess: (dono: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    // retorna adotador se tiver ou nulo se nao foi adotado
    fun getAdotador(
        animalId: String,
        onSuccess: (adotador: Usuario?) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun getAnimal(
        id: String,
        onSuccess: (animal: Animal) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun adicionarAnimal(
        animal: Animal,
        onSuccess: (id: String) -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun editarAnimal(
        animal: Animal,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    fun removerAnimal(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    // adotador busca animal
    fun animalBuscado(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )

    // dono busca animal
    fun animalEnviado(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    )
}
