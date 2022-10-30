package com.newhome.dao

import android.graphics.Bitmap
import com.newhome.dto.Animal
import com.newhome.dto.AnimalData
import com.newhome.dto.UsuarioData
import kotlinx.coroutines.Deferred

interface IAnimalProvider {
    suspend fun getImagemAnimal(id: String): Deferred<Bitmap>

    suspend fun getTodosAnimais(): Deferred<List<AnimalData>>

    suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalData>>

    suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalData>>

    suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalData>>

    suspend fun getDonoInicial(animalId: String): Deferred<UsuarioData>

    // retorna adotador se tiver ou nulo se nao foi adotado
    suspend fun getAdotador(animalId: String): Deferred<UsuarioData?>

    suspend fun getAnimal(id: String): Deferred<AnimalData>

    // returna id do animal
    suspend fun adicionarAnimal(animal: Animal): Deferred<String>

    suspend fun editarAnimal(animal: Animal): Deferred<Unit>

    suspend fun removerAnimal(id: String): Deferred<Unit>

    // adotador busca animal
    suspend fun animalBuscado(id: String): Deferred<Unit>

    // dono envia animal
    suspend fun animalEnviado(id: String): Deferred<Unit>
}
