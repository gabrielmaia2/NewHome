package com.newhome.services.concrete

import android.graphics.Bitmap
import com.newhome.dao.IAnimalProvider
import com.newhome.dao.IUsuarioProvider
import com.newhome.dto.Animal
import com.newhome.dto.AnimalAsync
import com.newhome.dto.UsuarioAsync
import com.newhome.services.IAnimalService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AnimalService(private val animalProvider: IAnimalProvider, private val usuarioProvider: IUsuarioProvider) :
    IAnimalService {
    override suspend fun getImagemAnimal(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.getImagemAnimal(id).await()
        }

    override suspend fun getTodosAnimais(): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.getTodosAnimais().await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.getAnimaisPostosAdocao(donoId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.getAnimaisAdotados(adotadorId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.getAnimaisSolicitados(solicitadorId).await().map { a ->
                AnimalAsync(a.id, a.nome, a.detalhes, animalProvider.getImagemAnimal(a.id))
            }
        }

    override suspend fun getDonoInicial(animalId: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.IO).async {
            val d = animalProvider.getDonoInicial(animalId).await()
            return@async UsuarioAsync(
                d.id, d.nome, d.detalhes, usuarioProvider.getImagemUsuario(d.id)
            )
        }

    override suspend fun getAdotador(animalId: String): Deferred<UsuarioAsync?> =
        CoroutineScope(Dispatchers.IO).async {
            val a = animalProvider.getAdotador(animalId).await() ?: return@async null
            return@async UsuarioAsync(
                a.id, a.nome, a.detalhes, usuarioProvider.getImagemUsuario(a.id)
            )
        }

    override suspend fun getAnimalSemImagem(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.IO).async {
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.nome, animal.detalhes)
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.IO).async {
            val imageTask = animalProvider.getImagemAnimal(id)
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.nome, animal.detalhes, imageTask)
        }

    override suspend fun adicionarAnimal(animal: Animal): Deferred<String> =
        CoroutineScope(Dispatchers.IO).async {
            if (animal.nome.isEmpty() || animal.nome.length > 64) {
                throw Exception("Nome deve ter entre 1 e 64 caracteres.")
            }
            if (animal.detalhes.length < 8 || animal.detalhes.length > 512) {
                throw Exception("Descrição deve ter entre 8 e 512 caracteres.")
            }

            return@async animalProvider.adicionarAnimal(animal).await()
        }

    override suspend fun editarAnimal(animal: Animal): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.editarAnimal(animal).await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.removerAnimal(id).await()
        }

    override suspend fun animalBuscado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.animalBuscado(id).await()
        }

    override suspend fun animalEnviado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            return@async animalProvider.animalEnviado(id).await()
        }
}
