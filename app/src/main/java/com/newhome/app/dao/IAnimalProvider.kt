package com.newhome.app.dao

import android.graphics.Bitmap
import com.newhome.app.dto.AnimalData
import com.newhome.app.dto.NewAnimalData
import com.newhome.app.dto.UpdateAnimalData
import kotlinx.coroutines.Deferred

interface IAnimalProvider {
    suspend fun getAllAnimals(): Deferred<List<AnimalData>>

    suspend fun getAnimalsIdsFromList(userId: String, listType: AnimalList): Deferred<List<String>>

    suspend fun getDonorId(animalId: String): Deferred<String>

    // retorna adotador se tiver ou nulo se nao foi adotado
    suspend fun getAdopterId(animalId: String): Deferred<String>

    suspend fun getAnimal(id: String): Deferred<AnimalData>

    // returna id do animal
    suspend fun criarAnimal(animal: NewAnimalData): Deferred<String>

    suspend fun editarAnimal(animal: UpdateAnimalData): Deferred<Unit>

    suspend fun removerAnimal(id: String): Deferred<Unit>

    // adotador marca animal como adotado
    suspend fun marcarAnimalAdotado(id: String): Deferred<Unit>
}
