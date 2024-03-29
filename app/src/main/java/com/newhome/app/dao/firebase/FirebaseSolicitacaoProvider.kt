package com.newhome.app.dao.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.newhome.app.dao.ISolicitacaoProvider
import com.newhome.app.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

open class FirebaseSolicitacaoProvider(
    private val db: FirebaseFirestore
) : ISolicitacaoProvider {
    private val sp = StoreProvider(db)

    override suspend fun <T> runTransaction(
        func: (Transaction) -> T
    ): Deferred<T> = sp.runTransaction(func)

    override suspend fun solicitarAnimal(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuario = FirebaseAuth.getInstance().currentUser!!

            val solicitadorRef = db.collection("usuarios").document(usuario.uid)
            val animalRef = db.collection("animais").document(animalId)

            val success = db.runTransaction { transaction ->
                val solicitadorData = transaction.get(solicitadorRef)
                val animalData = transaction.get(animalRef)

                if (!animalData.exists()) throw Exception("Animal does not exist.")

                var podeSolicitar = !(animalData.getBoolean("buscando")!!)
                podeSolicitar = podeSolicitar && animalData.getString("adotador")!! == ""

                if (podeSolicitar) {
                    val newSolicitados =
                        ArrayList<String>((solicitadorData.data!!["solicitados"] as List<*>).map { i -> i as String }).also {
                            it.add(animalId)
                        }

                    val newSolicitadores =
                        ArrayList<String>((animalData.data!!["solicitadores"] as List<*>).map { i -> i as String })
                    newSolicitadores.add(solicitadorData.id)

                    transaction.update(solicitadorRef, "solicitados", newSolicitados)
                    transaction.update(animalRef, "solicitadores", newSolicitadores)
                }

                podeSolicitar
            }.await()
            if (success) {
                return@async
            } else {
                throw Exception("Animal já está em processo de adoção")
            }
        }

    override suspend fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String
    ): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuariosRef = db.collection("usuarios")
            val solicitadorRef = usuariosRef.document(solicitacaoId.solicitadorID)
            val animalRef = db.collection("animais").document(solicitacaoId.animalID)

            db.runTransaction { transaction ->
                val solicitadorData = transaction.get(solicitadorRef)
                val animalData = transaction.get(animalRef)

                if (!animalData.exists()) throw Exception("Animal does not exist.")
                if (!solicitadorData.exists()) throw Exception("User does not exist.")

                val idsSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitadores = ArrayList<String>()
                newSolicitadores.add(solicitadorData.id)

                val solicitadoresRefs = ArrayList<DocumentReference>()
                val newSolicitadosList = ArrayList<List<*>>()

                for (solicitadorId in idsSolicitadores) {
                    val solicitador = transaction.get(usuariosRef.document(solicitadorId as String))
                    if (!solicitador.exists()) continue

                    val newSolicitados = (solicitador.data!!["solicitados"] as List<*>)
                        .filter { i -> i != animalData.id }

                    solicitadoresRefs.add(usuariosRef.document(solicitador.id))
                    newSolicitadosList.add(newSolicitados)
                }

                for (i in 0 until solicitadoresRefs.size) {
                    transaction.update(solicitadoresRefs[i], "solicitados", newSolicitadosList[i])
                }

                transaction.update(animalRef, "buscando", true)
                transaction.update(animalRef, "detalhesAdocao", detalhesAdocao)
                transaction.update(animalRef, "solicitadores", newSolicitadores)
            }.await()
        }

    override suspend fun rejeitarSolicitacao(solicitacaoId: SolicitacaoID): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuariosRef = db.collection("usuarios")
            val solicitadorRef = usuariosRef.document(solicitacaoId.solicitadorID)
            val animalRef = db.collection("animais").document(solicitacaoId.animalID)

            db.runTransaction { transaction ->
                val solicitadorData = transaction.get(solicitadorRef)
                val animalData = transaction.get(animalRef)

                if (!animalData.exists()) throw Exception("Animal does not exist.")
                if (!solicitadorData.exists()) throw Exception("User does not exist.")

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados = (solicitadorData.data!!["solicitados"] as List<*>)
                    .filter { i -> i != animalData.id }

                transaction.update(solicitadorRef, "solicitados", newSolicitados)

                transaction.update(animalRef, "buscando", false)
                transaction.update(animalRef, "detalhesAdocao", "")
                transaction.update(animalRef, "solicitadores", newSolicitadores)
            }.await()
        }

    override suspend fun cancelarSolicitacao(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuariosRef = db.collection("usuarios")
            val animalRef = db.collection("animais").document(animalId)

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val solicitadorRef =
                    usuariosRef.document((animalData.data!!["solicitadores"] as List<*>)[0] as String)
                val solicitadorData = transaction.get(solicitadorRef)

                if (!animalData.exists()) throw Exception("Animal does not exist.")
                if (!solicitadorData.exists()) throw Exception("User does not exist.")

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados = (solicitadorData.data!!["solicitados"] as List<*>)
                    .filter { i -> i != animalData.id }

                transaction.update(solicitadorRef, "solicitados", newSolicitados)

                transaction.update(animalRef, "buscando", false)
                transaction.update(animalRef, "detalhesAdocao", "")
                transaction.update(animalRef, "solicitadores", newSolicitadores)
            }.await()
        }

    override suspend fun cancelarSolicitacaoAceita(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuariosRef = db.collection("usuarios")
            val animalRef = db.collection("animais").document(animalId)

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val solicitadorRef =
                    usuariosRef.document((animalData.data!!["solicitadores"] as List<*>)[0] as String)
                val solicitadorData = transaction.get(solicitadorRef)

                if (!animalData.exists()) throw Exception("Animal does not exist.")
                if (!solicitadorData.exists()) throw Exception("User does not exist.")

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados = (solicitadorData.data!!["solicitados"] as List<*>)
                    .filter { i -> i != animalData.id }

                transaction.update(solicitadorRef, "solicitados", newSolicitados)

                transaction.update(animalRef, "buscando", false)
                transaction.update(animalRef, "detalhesAdocao", "")
                transaction.update(animalRef, "solicitadores", newSolicitadores)
            }.await()
        }
}