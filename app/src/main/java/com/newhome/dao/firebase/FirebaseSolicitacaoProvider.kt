package com.newhome.dao.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.ISolicitacaoProvider
import com.newhome.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class FirebaseSolicitacaoProvider : ISolicitacaoProvider {
    private val db = Firebase.firestore

    override suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewData>> =
        CoroutineScope(Dispatchers.Main).async {
        val usuario = FirebaseAuth.getInstance().currentUser!!

        val usuariosRef = db.collection("usuarios")
        val usuarioRef = usuariosRef.document(usuario.uid)
        val animaisRef = db.collection("animais")

        val solicitacoes = db.runTransaction { transaction ->
            val usuarioData = transaction.get(usuarioRef)
            val idsAnimais = usuarioData.data!!["animais"] as List<*>

            val solicitacoes = ArrayList<SolicitacaoPreviewData>()

            for (idAnimal in idsAnimais) {
                val animalRef = animaisRef.document(idAnimal as String)
                val animalData = transaction.get(animalRef)

                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
                for (idSolicitador in idsSolicitadores) {
                    val solicitadorRef = usuariosRef.document(idSolicitador as String)
                    val solicitadorData = transaction.get(solicitadorRef)

                    val solicitacaoId = SolicitacaoID(
                        idAnimal,
                        idSolicitador
                    )

                    val solicitacao = SolicitacaoPreviewData()
                    solicitacao.id = solicitacaoId
                    solicitacao.titulo = solicitadorData.data!!["nome"] as String
                    solicitacao.descricao = "Quer adotar ${animalData.data!!["nome"]}"

                    solicitacoes.add(solicitacao)
                }
            }

            solicitacoes
        }.await()

        return@async solicitacoes
    }

    override suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewData>> =
        CoroutineScope(Dispatchers.Main).async {
            val usuariosRef = db.collection("usuarios")
            val animaisRef = db.collection("animais")

            val solicitacoes = db.runTransaction { transaction ->
                val solicitacoes = ArrayList<SolicitacaoPreviewData>()

                val animalRef = animaisRef.document(animalId)
                val animalData = transaction.get(animalRef)

                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
                for (idSolicitador in idsSolicitadores) {
                    val solicitadorRef = usuariosRef.document(idSolicitador as String)
                    val solicitadorData = transaction.get(solicitadorRef)

                    val solicitacaoId = SolicitacaoID(
                        animalId,
                        idSolicitador
                    )

                    val solicitacao = SolicitacaoPreviewData(
                        solicitacaoId,
                        solicitadorData.data!!["nome"] as String,
                        "Quer adotar ${animalData.data!!["nome"]}"
                    )

                    solicitacoes.add(solicitacao)
                }

                solicitacoes
            }.await()

            return@async solicitacoes
    }

    override suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoData> =
        CoroutineScope(Dispatchers.Main).async {
            val solicitadorRef = db.collection("usuarios").document(solicitacaoId.solicitadorID)
            val animalRef = db.collection("animais").document(solicitacaoId.animalID)

            val solicitacao = db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)
                val solicitadorData = transaction.get(solicitadorRef)

                val animal = AnimalData(
                    animalData.id,
                    animalData.data!!["nome"] as String,
                    animalData.data!!["detalhes"] as String
                )

                val solicitador = UsuarioData(
                    solicitadorData.id,
                    solicitadorData.data!!["nome"] as String,
                    solicitadorData.data!!["detalhes"] as String
                )

                val solicitacao = SolicitacaoData(
                    solicitacaoId,
                    animal,
                    solicitador
                )

                solicitacao
            }.await()

            return@async solicitacao
    }

    override suspend fun getStatusSolicitacao(animalId : String): Deferred<StatusSolicitacao> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO corrigir esse status tem que ser so pro id do solicitador
            val animalRef = db.collection("animais").document(animalId)

            val status = db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val status = StatusSolicitacao(
                    (animalData.data!!["solicitadores"] as List<*>).isNotEmpty(),
                    animalData.getBoolean("buscando")!!,
                    animalData.getString("detalhesAdocao")!!
                )

                status
            }.await()

            return@async status
    }

    override suspend fun solicitarAnimal(animalId: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val usuario = FirebaseAuth.getInstance().currentUser!!

            val solicitadorRef = db.collection("usuarios").document(usuario.uid)
            val animalRef = db.collection("animais").document(animalId)

            val success = db.runTransaction { transaction ->
                val solicitadorData = transaction.get(solicitadorRef)
                val animalData = transaction.get(animalRef)

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
            }
            else {
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

                val idsSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitadores = ArrayList<String>()
                newSolicitadores.add(solicitadorData.id)

                val solicitadoresRefs = ArrayList<DocumentReference>()
                val newSolicitadosList = ArrayList<List<*>>()

                for (solicitadorId in idsSolicitadores) {
                    val solicitador = transaction.get(usuariosRef.document(solicitadorId as String))
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

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados =(solicitadorData.data!!["solicitados"] as List<*>)
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

                val solicitadorRef = usuariosRef.document((animalData.data!!["solicitadores"] as List<*>)[0] as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados =(solicitadorData.data!!["solicitados"] as List<*>)
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

                val solicitadorRef = usuariosRef.document((animalData.data!!["solicitadores"] as List<*>)[0] as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val newSolicitadores = (animalData.data!!["solicitadores"] as List<*>)
                    .filter { i -> i != solicitadorData.id }

                val newSolicitados =(solicitadorData.data!!["solicitados"] as List<*>)
                    .filter { i -> i != animalData.id }

                transaction.update(solicitadorRef, "solicitados", newSolicitados)

                transaction.update(animalRef, "buscando", false)
                transaction.update(animalRef, "detalhesAdocao", "")
                transaction.update(animalRef, "solicitadores", newSolicitadores)
            }.await()
    }
}