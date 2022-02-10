package com.newhome.dao.firebase

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.ISolicitacaoProvider
import com.newhome.dao.ImagemProvider
import com.newhome.dto.*

class FirebaseSolicitacaoProvider(val context: Context) : ISolicitacaoProvider {
    private val db = Firebase.firestore

    override fun getTodasSolicitacoes(
        onSuccess: (solicitacoes: List<SolicitacaoPreview>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser!!

        val usuariosRef = db.collection("usuarios")
        val usuarioRef = usuariosRef.document(usuario.uid)
        val animaisRef = db.collection("animais")

        db.runTransaction { transaction ->
            val usuarioData = transaction.get(usuarioRef)
            val idsAnimais = usuarioData.data!!["animais"] as List<*>

            val solicitacoes = ArrayList<SolicitacaoPreview>()

            for (idAnimal in idsAnimais) {
                val animalRef = animaisRef.document(idAnimal as String)
                val animalData = transaction.get(animalRef)

                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
                for (idSolicitador in idsSolicitadores) {
                    val solicitadorRef = usuariosRef.document(idSolicitador as String)
                    val solicitadorData = transaction.get(solicitadorRef)

                    val solicitacaoId = SolicitacaoID()
                    solicitacaoId.animalId = idAnimal
                    solicitacaoId.adotadorId = idSolicitador

                    val solicitacao = SolicitacaoPreview()
                    solicitacao.id = solicitacaoId
                    solicitacao.titulo = solicitadorData.data!!["nome"] as String
                    solicitacao.descricao = "Quer adotar ${animalData.data!!["nome"]}"

                    solicitacoes.add(solicitacao)
                }
            }

            solicitacoes
        }
            .addOnSuccessListener { solicitacoes ->
                onSuccess(solicitacoes)

                for (solicitacao in solicitacoes) {
                    val onLoadBitmap = { bitmap: Bitmap ->
                        solicitacao.imagemSolicitador = bitmap
                        onSuccess(solicitacoes)
                    }

                    ImagemProvider.getImageFromFirebase(
                        "usuarios/${solicitacao.id!!.adotadorId}",
                        { bitmap ->
                            onLoadBitmap(bitmap)
                        },
                        {
                            onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                        })
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun getTodasSolicitacoesAnimal(
        animalId: String,
        onSuccess: (solicitacoes: List<SolicitacaoPreview>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuariosRef = db.collection("usuarios")
        val animaisRef = db.collection("animais")

        db.runTransaction { transaction ->
            val solicitacoes = ArrayList<SolicitacaoPreview>()

            val animalRef = animaisRef.document(animalId)
            val animalData = transaction.get(animalRef)

            val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
            for (idSolicitador in idsSolicitadores) {
                val solicitadorRef = usuariosRef.document(idSolicitador as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val solicitacaoId = SolicitacaoID()
                solicitacaoId.animalId = animalId
                solicitacaoId.adotadorId = idSolicitador

                val solicitacao = SolicitacaoPreview()
                solicitacao.id = solicitacaoId
                solicitacao.titulo = solicitadorData.data!!["nome"] as String
                solicitacao.descricao = "Quer adotar ${animalData.data!!["nome"]}"

                solicitacoes.add(solicitacao)
            }

            solicitacoes
        }
            .addOnSuccessListener { solicitacoes ->
                onSuccess(solicitacoes)

                for (solicitacao in solicitacoes) {
                    val onLoadBitmap = { bitmap: Bitmap ->
                        solicitacao.imagemSolicitador = bitmap
                        onSuccess(solicitacoes)
                    }

                    ImagemProvider.getImageFromFirebase(
                        "usuarios/${solicitacao.id!!.adotadorId}",
                        { bitmap ->
                            onLoadBitmap(bitmap)
                        },
                        {
                            onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                        })
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun getSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: (solicitacao: Solicitacao) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val solicitadorRef = db.collection("usuarios").document(solicitacaoId.adotadorId)
        val animalRef = db.collection("animais").document(solicitacaoId.animalId)

        db.runTransaction { transaction ->
            val animalData = transaction.get(animalRef)
            val solicitadorData = transaction.get(solicitadorRef)

            val animal = Animal()
            animal.id = animalData.id
            animal.nome = animalData.data!!["nome"] as String
            animal.detalhes = animalData.data!!["detalhes"] as String

            val solicitador = Usuario()
            solicitador.id = solicitadorData.id
            solicitador.nome = solicitadorData.data!!["nome"] as String
            solicitador.detalhes = solicitadorData.data!!["detalhes"] as String

            val solicitacao = Solicitacao()
            solicitacao.id = solicitacaoId
            solicitacao.animal = animal
            solicitacao.solicitador = solicitador

            solicitacao
        }
            .addOnSuccessListener { solicitacao ->
                onSuccess(solicitacao)

                val onLoadSolicitador = { bitmap: Bitmap ->
                    solicitacao.solicitador.imagem = bitmap
                    onSuccess(solicitacao)
                }

                val onLoadAnimal = { bitmap: Bitmap ->
                    solicitacao.animal.imagem = bitmap
                    onSuccess(solicitacao)
                }

                ImagemProvider.getImageFromFirebase(
                    "usuarios/${solicitacao.solicitador.id}",
                    { bitmap ->
                        onLoadSolicitador(bitmap)
                    },
                    {
                        onLoadSolicitador(ImagemProvider.getDefaultBitmap(context))
                    })

                ImagemProvider.getImageFromFirebase("animais/${solicitacao.animal.id}", { bitmap ->
                    onLoadAnimal(bitmap)
                }, {
                    onLoadAnimal(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun getStatusSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: (status: StatusSolicitacao) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val animalRef = db.collection("animais").document(solicitacaoId.animalId)

        db.runTransaction { transaction ->
            val animalData = transaction.get(animalRef)

            val status = StatusSolicitacao()
            status.solicitado = (animalData.data!!["solicitadores"] as List<*>).size > 0
            status.solicitacaoAceita = animalData.getBoolean("buscando")!!
            status.detalhesAdocao = animalData.getString("detalhesAdocao")!!

            status
        }
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    override fun solicitarAnimal(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser!!

        val solicitadorRef = db.collection("usuarios").document(usuario.uid)
        val animalRef = db.collection("animais").document(animalId)

        db.runTransaction { transaction ->
            val solicitadorData = transaction.get(solicitadorRef)
            val animalData = transaction.get(animalRef)

            val solicitacaoId = SolicitacaoID()
            solicitacaoId.adotadorId = animalData.getString("dono")!!
            solicitacaoId.animalId = animalId

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
        }
            .addOnSuccessListener { success ->
                if (success) {
                    onSuccess()
                }
                else {
                    onFailure(Exception("Não pode mais solicitar animal."))
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuariosRef = db.collection("usuarios")
        val solicitadorRef = usuariosRef.document(solicitacaoId.adotadorId)
        val animalRef = db.collection("animais").document(solicitacaoId.animalId)

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
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }

    override fun rejeitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuariosRef = db.collection("usuarios")
        val solicitadorRef = usuariosRef.document(solicitacaoId.adotadorId)
        val animalRef = db.collection("animais").document(solicitacaoId.animalId)

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
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }

    override fun cancelarSolicitacao(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
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
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }

    override fun cancelarSolicitacaoAceita(
        animalId: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
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
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }
}
