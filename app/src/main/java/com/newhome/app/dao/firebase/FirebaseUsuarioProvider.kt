package com.newhome.app.dao.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.newhome.app.dao.AnimalList
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dto.NewUser
import com.newhome.app.dto.UserData
import com.newhome.app.utils.Utils.Companion.toStringArrayList
import kotlinx.coroutines.Deferred

open class FirebaseUsuarioProvider(
    private val db: FirebaseFirestore
) : IUsuarioProvider {
    private val sp = StoreProvider(db)

    override suspend fun <T> runTransaction(
        func: (Transaction) -> T
    ): Deferred<T> = sp.runTransaction(func)

    private fun snapshotToUserData(snapshot: DocumentSnapshot): UserData {
        // TODO use this
        return UserData(
            snapshot.id,
            (snapshot.data?.get("nome") ?: "") as String,
            (snapshot.data?.get("detalhes") ?: "") as String
        )
    }

    override fun getUser(t: Transaction, id: String): UserData? {
            val doc = db.collection("usuarios").document(id)
            val snap = t.get(doc)

            if (!snap.exists()) return null
            return snapshotToUserData(snap)
        }

    override fun createUser(t: Transaction, usuario: NewUser) {
            val data = hashMapOf(
                "nome" to usuario.name,
                "detalhes" to usuario.details,
                "idade" to usuario.age,
                "animais" to ArrayList<String>(),
                "adotados" to ArrayList<String>(),
                "solicitados" to ArrayList<String>()
            )

            val doc = db.collection("usuarios").document(usuario.id)
            t.set(doc, data)
        }

    override fun updateUser(t: Transaction, usuario: UserData) {
            val doc = db.collection("usuarios").document(usuario.id)

            // TODO editar idade
            val docData = hashMapOf<String, Any>(
                "nome" to usuario.name,
                "detalhes" to usuario.details,
            )

            t.update(doc, docData)
        }

    override fun deleteUser(t: Transaction, id: String) {
            // TODO implementar
        }

    override fun getAnimalList(
        t: Transaction,
        id: String,
        list: AnimalList
    ): ArrayList<String>? {
            val donoRef = db.collection("usuarios").document(id)
            val donoData = t.get(donoRef)

            if (!donoData.exists()) return null
            return (donoData.data!!.get(list.toString()) as List<*>?).toStringArrayList()
        }

    override fun setAnimalList(
        t: Transaction,
        id: String,
        list: AnimalList,
        data: List<String>
    ) {
            val donoRef = db.collection("usuarios").document(id)
            t.update(donoRef, list.toString(), data)
        }
}
