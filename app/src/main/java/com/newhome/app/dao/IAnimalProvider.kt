package com.newhome.app.dao

import com.google.firebase.firestore.Transaction
import com.newhome.app.dto.AnimalData
import com.newhome.app.dto.NewAnimalData
import com.newhome.app.dto.UpdateAnimalData
import kotlinx.coroutines.Deferred

interface IAnimalProvider : IStoreProvider {
    suspend fun getAllAnimals(): Deferred<List<AnimalData>>

    suspend fun getAnimalsIdsFromList(
        userId: String,
        listType: AnimalList
    ): Deferred<List<String>>

    fun getDonorId(t: Transaction, animalId: String): String

    // retorna adotador se tiver ou nulo se nao foi adotado
    fun getAdopterId(t: Transaction, animalId: String): String

    fun getAnimal(t: Transaction, id: String): AnimalData?

    // returna id do animal
    fun criarAnimal(t: Transaction, animal: NewAnimalData): String

    fun editarAnimal(t: Transaction, animal: UpdateAnimalData)

    fun removerAnimal(t: Transaction, id: String)

    // adotador marca animal como adotado
    fun marcarAnimalAdotado(t: Transaction, id: String)
}
