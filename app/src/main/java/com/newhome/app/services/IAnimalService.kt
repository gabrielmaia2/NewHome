package com.newhome.app.services

import android.graphics.Bitmap
import com.newhome.app.dto.Animal
import com.newhome.app.dto.AnimalAsync
import com.newhome.app.dto.UsuarioAsync
import kotlinx.coroutines.Deferred

interface IAnimalService {
    suspend fun getImagemAnimal(id: String): Deferred<Bitmap>

    suspend fun getTodosAnimais(): Deferred<List<AnimalAsync>>

    suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalAsync>>

    suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalAsync>>

    suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalAsync>>

    suspend fun getDonoInicial(animalId: String): Deferred<UsuarioAsync>

    // retorna adotador se tiver ou nulo se nao foi adotado
    suspend fun getAdotador(animalId: String): Deferred<UsuarioAsync?>

    suspend fun getAnimalSemImagem(id: String): Deferred<AnimalAsync>

    suspend fun getAnimal(id: String): Deferred<AnimalAsync>

    // returna id do animal
    suspend fun adicionarAnimal(animal: Animal): Deferred<String>

    suspend fun editarAnimal(animal: Animal): Deferred<Unit>

    suspend fun removerAnimal(id: String): Deferred<Unit>

    // adotador busca animal
    suspend fun animalBuscado(id: String): Deferred<Unit>

    // dono envia animal
    suspend fun animalEnviado(id: String): Deferred<Unit>
}
