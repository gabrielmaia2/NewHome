package com.newhome.app.services.concrete

import android.graphics.Bitmap
import com.google.firebase.firestore.Transaction
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
        imageProvider.getAnimalImage(id)

    override suspend fun getTodosAnimais(): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            return@async animalProvider.getAllAnimals().await().map { a ->
                AnimalAsync(a.id, a.name, a.details, getImagemAnimal(a.id))
            }
        }

    suspend fun getAnimalsFromUserList(
        userId: String,
        listType: AnimalList
    ): Deferred<List<AnimalAsync>> =
        CoroutineScope(Dispatchers.Main).async {
            val animaisIds = animalProvider.getAnimalsIdsFromList(userId, listType)

            val tasks = ArrayList<Deferred<AnimalAsync?>>()

            for (id in animaisIds.await()) {
                tasks.add(CoroutineScope(Dispatchers.Main).async async1@ {
                    val a = getAnimal(id).await()
                    if (a == null) return@async1 null
                    AnimalAsync(a.id, a.name, a.details, getImagemAnimal(a.id))
                })
            }

            val l = tasks.awaitAll()
            val res = l.filter { a -> a != null }.map { a -> a!! }
            res.forEach { a -> if (a.id == AnimalAsync.empty.id) a.getImage?.cancelAndJoin() }

            return@async res
        }

    override suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalAsync>> =
        getAnimalsFromUserList(donoId, AnimalList.placedForAdoption)

    override suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalAsync>> =
        getAnimalsFromUserList(adotadorId, AnimalList.adopted)

    override suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalAsync>> =
        getAnimalsFromUserList(solicitadorId, AnimalList.adoptionRequested)

    override suspend fun getDonoInicial(animalId: String): Deferred<UsuarioAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val d = animalProvider.runTransaction { t ->
                val donorId = animalProvider.getDonorId(t, animalId)
                return@runTransaction usuarioProvider.getUser(t, donorId)
            }.await()

            if (d != null) return@async UsuarioAsync(
                d.id, d.name, d.details, imageProvider.getUserImage(d.id)
            )
            return@async null
        }

    override suspend fun getAdotador(animalId: String): Deferred<UsuarioAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val a = animalProvider.runTransaction { t ->
                val adopterId = animalProvider.getAdopterId(t, animalId)
                if (adopterId == "") return@runTransaction null

                return@runTransaction usuarioProvider.getUser(t, adopterId)
            }.await()

            if (a != null) return@async UsuarioAsync(
                a.id, a.name, a.details, imageProvider.getUserImage(a.id)
            )
            return@async null
        }

    override suspend fun getAnimalSemImagem(id: String): Deferred<AnimalAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val animal = animalProvider.runTransaction { t ->
                return@runTransaction animalProvider.getAnimal(t, id)
            }.await()
            if (animal != null) return@async AnimalAsync(animal.id, animal.name, animal.details)
            return@async null
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalAsync?> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = getImagemAnimal(id)
            val animal = animalProvider.runTransaction { t ->
                animalProvider.getAnimal(t, id)
            }.await()
            if (animal != null) return@async AnimalAsync(
                animal.id,
                animal.name,
                animal.details,
                imageTask
            )
            return@async null
        }

    override suspend fun adicionarAnimal(animal: Animal): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            lateinit var imageTask: Deferred<Unit>
            val aid = animalProvider.runTransaction { t ->
                if (animal.name.isEmpty() || animal.name.length > 64) {
                    throw Exception("Nome deve ter entre 1 e 64 caracteres.")
                }
                if (animal.details.length < 8 || animal.details.length > 512) {
                    throw Exception("Descrição deve ter entre 8 e 512 caracteres.")
                }

                val uid = contaProvider.getContaID()!!
                val newList = usuarioProvider.getAnimalList(t, uid, AnimalList.placedForAdoption)!!

                val a = NewAnimalData(animal.name, animal.details, uid)
                val aid = animalProvider.criarAnimal(t, a)
                newList.add(aid)

                usuarioProvider.setAnimalList(t, uid, AnimalList.placedForAdoption, newList)

                return@runTransaction aid
            }.await()

            imageProvider.saveAnimalImage(aid, animal.image).await()

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

            animalProvider.runTransaction { t ->
                animalProvider.editarAnimal(t, a)
            }.await()
            imageProvider.saveAnimalImage(animal.id, animal.image).await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val imageTask = imageProvider.removeImage("animais/${id}")

            animalProvider.runTransaction { t ->
                runBlocking {
                    val animal = animalProvider.getAnimal(t, id)
                    if (animal == null)
                        throw NoSuchElementException("Couldn't find animal with specified ID.")

                    var dList: List<String>? = usuarioProvider.getAnimalList(t, animal.donorId, AnimalList.placedForAdoption)

                    var aList: List<String>? = null
                    if (animal.adopterId.isNotEmpty()){
                        aList = usuarioProvider.getAnimalList(t, animal.adopterId, AnimalList.adopted)
                    }

                    val rLists = ArrayList<Pair<String, List<String>>>()
                    for (requesterId in animal.requestersIds) {
                        val rList: List<String>? = usuarioProvider.getAnimalList(t, requesterId, AnimalList.adoptionRequested)
                        if (rList != null) {
                            rLists.add(Pair(requesterId, rList))
                        }
                    }

                    if (dList != null){
                        dList = dList.filter { i -> i != id }
                        usuarioProvider.setAnimalList(t, animal.donorId, AnimalList.placedForAdoption, dList)
                    }

                    if (aList != null) {
                        aList = aList.filter { i -> i != id }
                        usuarioProvider.setAnimalList(t, animal.adopterId, AnimalList.adopted, aList)
                    }

                    for ((requesterId, rList) in rLists) {
                        val newRList = rList.filter { i -> i != id }
                        usuarioProvider.setAnimalList(t, requesterId, AnimalList.adoptionRequested, newRList)
                    }

                    animalProvider.removerAnimal(t, id)
                }
            }.await()

            imageTask.await()
        }

    override suspend fun animalBuscado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            animalProvider.runTransaction { t ->
                val animal = animalProvider.getAnimal(t, id)
                if (animal == null)
                    throw NoSuchElementException("Couldn't find animal with specified ID.")

                val requesterId = animal.requestersIds[0]

                // TODO usar solicitacaoprovider.verificaestabuscando e remover do getanimal
                if (!animal.beingAdopted) {
                    throw Exception("There is no accepted request.")
                }

                val rAdopted = usuarioProvider.getAnimalList(t, requesterId, AnimalList.adopted)
                var rAdoptionR: List<String>? =
                    usuarioProvider.getAnimalList(t, requesterId, AnimalList.adoptionRequested)

                if (rAdopted != null && rAdoptionR != null){
                    rAdopted.add(id)
                    usuarioProvider.setAnimalList(t, requesterId, AnimalList.adopted, rAdopted)

                    rAdoptionR = rAdoptionR.filter { i -> i != id }
                    usuarioProvider.setAnimalList(
                        t,
                        requesterId,
                        AnimalList.adoptionRequested,
                        rAdoptionR
                    )
                }

                animalProvider.marcarAnimalAdotado(t, id)
            }.await()
        }
}

