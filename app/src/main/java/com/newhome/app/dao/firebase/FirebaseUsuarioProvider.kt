package com.newhome.app.dao.firebase

import android.graphics.Bitmap
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.newhome.app.dao.AnimalList
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dto.NewUser
import com.newhome.app.dto.UserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class FirebaseUsuarioProvider(
    private val db: FirebaseFirestore
) : IUsuarioProvider {
    private fun snapshotToUserData(snapshot: DocumentSnapshot): UserData {
        // TODO use this
        return UserData(
            snapshot.id,
            (snapshot.data?.get("nome") ?: "") as String,
            (snapshot.data?.get("detalhes") ?: "") as String
        )
    }

    override suspend fun getUser(id: String): Deferred<UserData> =
        CoroutineScope(Dispatchers.Main).async {
            val doc = db.collection("usuarios").document(id).get().await()

            val exists = doc.exists()
            if (!exists) throw Exception("User does not exist.")

            return@async UserData(
                doc.id,
                doc.data!!["nome"] as String,
                doc.data!!["detalhes"] as String
            )
        }

    override suspend fun createUser(usuario: NewUser): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val data = hashMapOf(
                "nome" to usuario.name,
                "detalhes" to usuario.details,
                "idade" to usuario.age,
                "animais" to ArrayList<String>(),
                "adotados" to ArrayList<String>(),
                "solicitados" to ArrayList<String>()
            )

            db.collection("usuarios").document(usuario.id).set(data).await()
        }

    override suspend fun updateUser(usuario: UserData): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val exists = db.collection("usuarios").document(usuario.id).get().await().exists()
            if (!exists) throw Exception("User does not exist.")

            // TODO editar idade
            val docData = hashMapOf(
                "nome" to usuario.name,
                "detalhes" to usuario.details,
            )

            db.collection("usuarios").document(usuario.id).set(docData, SetOptions.merge()).await()
        }

    override suspend fun deleteUser(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO implementar
        }

    override suspend fun addAnimalIdToList(
        id: String,
        animalId: String,
        list: AnimalList
    ): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val donoRef = db.collection("usuarios").document(id)

            db.runTransaction { transaction ->
                val donoData = transaction.get(donoRef)

                if (!donoData.exists()) throw NoSuchElementException("Couldn't find user with specified ID.")

                val data = donoData.data?.get(list.toString()) ?: ArrayList<String>()
                val newList = ArrayList<String>((data as List<*>).map { i -> i.toString() })
                newList.add(animalId)

                transaction.update(donoRef, list.toString(), newList)
            }
        }

    override suspend fun removeAnimalIdFromList(
        id: String,
        animalId: String,
        list: AnimalList
    ): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val donoRef = db.collection("usuarios").document(id)

            db.runTransaction { transaction ->
                val donoData = transaction.get(donoRef)

                if (!donoData.exists()) throw NoSuchElementException("Couldn't find user with specified ID.")

                val data = donoData.data?.get(list.toString()) ?: ArrayList<String>()
                val l = ArrayList<String>((data as List<*>).map { i -> i.toString() })
                val newList = l.filter { i -> i != animalId }

                transaction.update(donoRef, list.toString(), newList)
            }
        }
}
