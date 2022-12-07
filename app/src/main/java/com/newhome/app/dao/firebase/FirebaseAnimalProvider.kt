package com.newhome.app.dao.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.newhome.app.dao.AnimalList
import com.newhome.app.dao.IAnimalProvider
import com.newhome.app.dto.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

open class FirebaseAnimalProvider(
    private val db: FirebaseFirestore
) : IAnimalProvider {
    private val sp = StoreProvider(db)

    override suspend fun <T> runTransaction(
        func: (Transaction) -> T
    ): Deferred<T> = sp.runTransaction(func)

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

    override fun getDonorId(t: Transaction, animalId: String): String {
            val doc = db.collection("animais").document(animalId)
            val docRef = t.get(doc)
            return (docRef.data?.get("dono") ?: "") as String
        }

    override fun getAdopterId(t: Transaction, animalId: String): String {
            val doc = db.collection("animais").document(animalId)
            val docRef = t.get(doc)
            return (docRef.data?.get("adotador") ?: "") as String
        }

    override fun getAnimal(t: Transaction, id: String): AnimalData? {
            val doc = db.collection("animais").document(id)
            val docRef = t.get(doc)

            if (!docRef.exists()) return null
            return snapshotToAnimalData(docRef)
        }

    override fun criarAnimal(t: Transaction, animal: NewAnimalData): String {
            val doc = db.collection("animais").document()

            val docData = hashMapOf(
                "nome" to animal.name,
                "detalhes" to animal.details,
                "dono" to animal.donorId,
                "adotador" to "",
                "solicitadores" to ArrayList<String>(),
                "buscando" to false,
                "detalhesAdocao" to ""
            )

            t.set(doc, docData)
            return doc.id
        }

    override fun editarAnimal(t: Transaction, animal: UpdateAnimalData){
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(animal.id)

            val docData = hashMapOf<String, Any>(
                "nome" to animal.name,
                "detalhes" to animal.details,
                "buscando" to animal.beingAdopted,
            )

            t.update(animalRef, docData)
        }

    override fun removerAnimal(t: Transaction, id: String){
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(id)
            t.delete(animalRef)
        }

    override fun marcarAnimalAdotado(t: Transaction, id: String) {
            // TODO verificar se e dono
            val animalRef = db.collection("animais").document(id)
            t.update(animalRef, "buscando", true)
        }
}
