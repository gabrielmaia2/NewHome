package com.newhome.app.dao.firebase

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.dao.AnimalList
import com.newhome.app.dao.IAnimalProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dto.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class FirebaseAnimalProvider(
    private val db: FirebaseFirestore
) : IAnimalProvider {
    private fun snapshotToAnimalData(snapshot: DocumentSnapshot): AnimalData {
        return AnimalData(
            snapshot.id,
            (snapshot.data?.get("nome") ?: "") as String,
            (snapshot.data?.get("detalhes") ?: "") as String,
            (snapshot.data?.get("dono") ?: "") as String,
            (snapshot.data?.get("adotador") ?: "") as String,
            (snapshot.data?.get("solicitadores") as List<*>).map { s -> s.toString() },
            (snapshot.data?.get("buscando") ?: false) as Boolean
        )
    }

    override suspend fun getAllAnimals(): Deferred<List<AnimalData>> =
        CoroutineScope(Dispatchers.Main).async {
            val docs = db.collection("animais").get().await()
            return@async docs.map { doc -> snapshotToAnimalData(doc) }
        }

    override suspend fun getAnimalsIdsFromList(
        userId: String,
        listType: AnimalList
    ): Deferred<List<String>> =
        CoroutineScope(Dispatchers.Main).async {
            val animaisIds = db.collection("usuarios").document(userId).get().await()

            val l = animaisIds.data!![listType.toString()] as List<*>
            return@async l.map { t -> t.toString() }
        }

    override suspend fun getDonorId(animalId: String): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("animais").document(animalId).get().await()
            return@async (docRef.data?.get("dono") ?: "") as String
        }

    override suspend fun getAdopterId(animalId: String): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("animais").document(animalId).get().await()
            return@async (docRef.data?.get("adotador") ?: "") as String
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalData> =
        CoroutineScope(Dispatchers.Main).async {
            val doc = db.collection("animais").document(id).get().await()

            if (!doc.exists()) return@async AnimalData.empty

            return@async snapshotToAnimalData(doc)
        }

    override suspend fun criarAnimal(animal: NewAnimalData): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            val animalRef = db.collection("animais").document()

            val docData = hashMapOf(
                "nome" to animal.name,
                "detalhes" to animal.details,
                "dono" to animal.donorId,
                "adotador" to "",
                "solicitadores" to ArrayList<String>(),
                "buscando" to false,
                "detalhesAdocao" to ""
            )

            animalRef.set(docData).await()

            return@async animalRef.id
        }

    override suspend fun editarAnimal(animal: UpdateAnimalData): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(animal.id)

            val docData = hashMapOf(
                "nome" to animal.name,
                "detalhes" to animal.details,
                "buscando" to animal.beingAdopted,
            )

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                if (!animalData.exists())
                    throw NoSuchElementException("Couldn't find animal with specified ID.")

                transaction.set(animalRef, docData, SetOptions.merge())
            }.await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(id)

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                if (!animalData.exists())
                    throw NoSuchElementException("Couldn't find animal with specified ID.")

                transaction.delete(animalRef)
            }.await()
        }

    override suspend fun marcarAnimalAdotado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(id)

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                if (!animalData.exists())
                    throw NoSuchElementException("Couldn't find animal with specified ID.")

                val data = hashMapOf(
                    "buscando" to true
                )
                transaction.set(animalRef, data, SetOptions.merge())
            }.await()
        }
}
