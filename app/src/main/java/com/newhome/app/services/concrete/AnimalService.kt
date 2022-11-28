package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.newhome.app.dao.*
import com.newhome.app.dto.*
import com.newhome.app.services.IAnimalService
import kotlinx.coroutines.*

class AnimalService(
    private val animalProvider: IAnimalProvider,
    private val usuarioProvider: IUsuarioProvider,
    private val contaProvider: IContaProvider,
    private val imageProvider: IImageProvider
) :
    IAnimalService {
    override suspend fun getImagemAnimal(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAnimalImage(id).await()
        }

    override suspend fun getTodosAnimais(): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAllAnimals().await().map { a ->
                AnimalAsync(a.id, a.name, a.details, animalProvider.getAnimalImage(a.id))
            }
        }

    suspend fun getAnimalsFromUserList(
        userId: String,
        listType: AnimalList
    ): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            val animaisIds = animalProvider.getAnimalsIdsFromList(userId, listType)

            val tasks = ArrayList<Deferred<AnimalAsync>>()

            for (id in animaisIds.await()) {
                tasks.add(CoroutineScope(Dispatchers.Main).async {
                    val a = getAnimal(id).await()
                    AnimalAsync(a.id, a.name, a.details, animalProvider.getAnimalImage(a.id))
                })
            }

            val res = tasks.awaitAll()
            res.forEach { a -> if (a.id == AnimalAsync.empty.id) a.getImage?.cancelAndJoin() }

            return@async res.filter { a -> a.id != AnimalAsync.empty.id }
        }

    override suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async getAnimalsFromUserList(donoId, AnimalList.placedForAdoption).await()
        }

    override suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async getAnimalsFromUserList(adotadorId, AnimalList.adopted).await()
        }

    override suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async getAnimalsFromUserList(solicitadorId, AnimalList.adoptionRequested).await()
        }

    override suspend fun getDonoInicial(animalId: String): Deferred<UsuarioAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val donorId = animalProvider.getDonorId(animalId).await()
            val d = usuarioProvider.getUser(donorId).await()
            return@async UsuarioAsync(
                d.id, d.name, d.details, usuarioProvider.getUserImage(d.id)
            )
        }

    override suspend fun getAdotador(animalId: String): Deferred<UsuarioAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val adopterId = animalProvider.getAdopterId(animalId).await()
            if (adopterId == "") return@async null

            val a = usuarioProvider.getUser(adopterId).await()
            return@async UsuarioAsync(
                a.id, a.name, a.details, usuarioProvider.getUserImage(a.id)
            )
        }

    override suspend fun getAnimalSemImagem(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.name, animal.details)
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalAsync> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = animalProvider.getAnimalImage(id)
            val animal = animalProvider.getAnimal(id).await()
            return@async AnimalAsync(animal.id, animal.name, animal.details, imageTask)
        }

    override suspend fun adicionarAnimal(animal: Animal): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            if (animal.name.isEmpty() || animal.name.length > 64) {
                throw Exception("Nome deve ter entre 1 e 64 caracteres.")
            }
            if (animal.details.length < 8 || animal.details.length > 512) {
                throw Exception("Descrição deve ter entre 8 e 512 caracteres.")
            }

            val uid = contaProvider.getContaID()!!
            val a = NewAnimalData(animal.name, animal.details, uid)

            val aid = animalProvider.criarAnimal(a).await()

            val task1 = usuarioProvider.addAnimalIdToList(uid, aid, AnimalList.placedForAdoption)
            val task2 = imageProvider.saveImage("animais/${aid}", animal.image)

            task1.await()
            task2.await()

            return@async aid
        }

    override suspend fun editarAnimal(animal: Animal): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            if (animal.name.isEmpty() || animal.name.length > 64) {
                throw Exception("Nome deve ter entre 1 e 64 caracteres.")
            }
            if (animal.details.length < 8 || animal.details.length > 512) {
                throw Exception("Descrição deve ter entre 8 e 512 caracteres.")
            }

            val a = UpdateAnimalData(animal.id, animal.name, animal.details)

            val task1 = animalProvider.editarAnimal(a)
            val task2 = imageProvider.saveImage("animais/${animal.id}", animal.image)

            task1.await()
            task2.await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = imageProvider.removeImage("animais/${id}")

            val animal = animalProvider.getAnimal(id).await()
            animalProvider.removerAnimal(id).await()

            val tasks = arrayListOf(
                usuarioProvider.removeAnimalIdFromList(
                    animal.donorId,
                    id,
                    AnimalList.placedForAdoption
                )
            )

            if (animal.adopterId.isNotEmpty())
                tasks.add(
                    usuarioProvider.removeAnimalIdFromList(
                        animal.adopterId,
                        id,
                        AnimalList.adopted
                    )
                )

            for (requesterId in animal.requestersIds) {
                tasks.add(
                    usuarioProvider.removeAnimalIdFromList(
                        requesterId,
                        id,
                        AnimalList.adoptionRequested
                    )
                )
            }

            tasks.map { t ->
                async {
                    try {
                        t.await()
                    } catch (_: NoSuchElementException) {
                    }
                }
            }

            tasks.awaitAll()
            imageTask.await()
        }

    override suspend fun animalBuscado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val animal = animalProvider.getAnimal(id).await()
            val requesterId = animal.requestersIds[0]
            val requester = usuarioProvider.getUser(requesterId).await()

            // TODO check animal.exists
            if (animal.id == "0")
                throw NoSuchElementException("Couldn't find animal with specified ID.")

            // TODO usar solicitacaoprovider.verificaestabuscando e remover do getanimal
            if (!animal.beingAdopted) {
                throw Exception("There is no accepted request.")
            }

            usuarioProvider.removeAnimalIdFromList(
                requesterId,
                id,
                AnimalList.adoptionRequested
            ).await()

            usuarioProvider.addAnimalIdToList(
                requesterId,
                id,
                AnimalList.adopted
            ).await()

            animalProvider.marcarAnimalAdotado(id).await()
        }
}
