package com.newhome.app.dao.firebase

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.app.dao.IAnimalProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dto.Animal
import com.newhome.app.dto.AnimalData
import com.newhome.app.dto.UsuarioData
import com.newhome.app.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class FirebaseAnimalProvider(private val imageProvider: IImageProvider) : IAnimalProvider {
    private val db = Firebase.firestore

    override suspend fun getImagemAnimal(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async imageProvider.getImageOrDefault("animais/${id}").await()
        }

    override suspend fun getTodosAnimais(): Deferred<List<AnimalData>> =
        CoroutineScope(Dispatchers.Main).async {
            val docs = db.collection("animais").get().await()

            val animais = ArrayList<AnimalData>()
            for (doc in docs) {
                val animal = AnimalData(
                    doc.id,
                    doc.data["nome"] as String,
                    doc.data["detalhes"] as String
                )

                animais.add(animal)
            }
            return@async animais
        }

    override suspend fun getAnimaisPostosAdocao(donoId: String): Deferred<List<AnimalData>> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("usuarios").document(donoId)
            val animaisRef = db.collection("animais")

            val animais = db.runTransaction { transaction ->
                val animais = ArrayList<AnimalData>()

                val snapshot = transaction.get(docRef)
                val idsAnimais = snapshot.data!!["animais"] as List<*>

                for (id in idsAnimais) {
                    val animalRef = animaisRef.document(id as String)
                    val animalData = transaction.get(animalRef)

                    val animal = AnimalData(
                        animalData.id,
                        animalData.data!!["nome"] as String,
                        animalData.data!!["detalhes"] as String
                    )

                    animais.add(animal)
                }

                animais
            }.await()

            return@async animais
        }

    override suspend fun getAnimaisAdotados(adotadorId: String): Deferred<List<AnimalData>> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("usuarios").document(adotadorId)
            val animaisRef = db.collection("animais")

            val animais = db.runTransaction { transaction ->
                val animais = ArrayList<AnimalData>()

                val snapshot = transaction.get(docRef)
                val idsAnimais = snapshot.data!!["adotados"] as List<*>

                for (id in idsAnimais) {
                    val animalRef = animaisRef.document(id as String)
                    val animalData = transaction.get(animalRef)

                    val animal = AnimalData(
                        animalData.id,
                        animalData.data!!["nome"] as String,
                        animalData.data!!["detalhes"] as String
                    )

                    animais.add(animal)
                }

                animais
            }.await()

            return@async animais
        }

    override suspend fun getAnimaisSolicitados(solicitadorId: String): Deferred<List<AnimalData>> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("usuarios").document(solicitadorId)
            val animaisRef = db.collection("animais")

            val animais = db.runTransaction { transaction ->
                val animais = ArrayList<AnimalData>()

                val snapshot = transaction.get(docRef)
                val idsAnimais = snapshot.data!!["solicitados"] as List<*>

                for (id in idsAnimais) {
                    val animalRef = animaisRef.document(id as String)
                    val animalData = transaction.get(animalRef)

                    val animal = AnimalData(
                        animalData.id,
                        animalData.data!!["nome"] as String,
                        animalData.data!!["detalhes"] as String
                    )

                    animais.add(animal)
                }

                animais
            }.await()

            return@async animais
        }

    override suspend fun getDonoInicial(animalId: String): Deferred<UsuarioData> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("animais").document(animalId)
            val usuariosRef = db.collection("usuarios")

            val dono = db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val donoId = snapshot.getString("dono")!!

                val donoRef = usuariosRef.document(donoId)
                val donoData = transaction.get(donoRef)

                val dono = UsuarioData(
                    donoData.id,
                    donoData.data!!["nome"] as String,
                    donoData.data!!["detalhes"] as String
                )

                dono
            }.await()

            return@async dono
        }

    override suspend fun getAdotador(animalId: String): Deferred<UsuarioData?> =
        CoroutineScope(Dispatchers.Main).async {
            val docRef = db.collection("animais").document(animalId)
            val usuariosRef = db.collection("usuarios")

            val adotador = db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val adotadorId = snapshot.getString("adotador")!!

                if (adotadorId == "") {
                    return@runTransaction null
                }

                val adotadorRef = usuariosRef.document(adotadorId)
                val adotadorData = transaction.get(adotadorRef)

                val dono = UsuarioData(
                    adotadorData.id,
                    adotadorData.data!!["nome"] as String,
                    adotadorData.data!!["detalhes"] as String
                )

                dono
            }.await() ?: return@async null

            return@async adotador
        }

    override suspend fun getAnimal(id: String): Deferred<AnimalData> =
        CoroutineScope(Dispatchers.Main).async {
            val doc = db.collection("animais").document(id)
                .get()
                .await()

            return@async AnimalData(
                doc.id,
                doc.data!!["nome"] as String,
                doc.data!!["detalhes"] as String
            )
        }

    override suspend fun adicionarAnimal(animal: Animal): Deferred<String> =
        CoroutineScope(Dispatchers.Main).async {
            val usuario = FirebaseAuth.getInstance().currentUser!!

            val animalRef = db.collection("animais").document()
            val donoRef = db.collection("usuarios").document(usuario.uid)

            val docRef = db.runTransaction { transaction ->
                val docData = hashMapOf(
                    "nome" to animal.nome,
                    "detalhes" to animal.detalhes,
                    "dono" to usuario.uid,
                    "adotador" to "",
                    "solicitadores" to ArrayList<String>(),
                    "buscando" to false,
                    "detalhesAdocao" to ""
                )

                val donoData = transaction.get(donoRef)
                val novoAnimais =
                    ArrayList<String>((donoData.data!!["animais"] as List<*>).map { i -> i as String })
                novoAnimais.add(animalRef.id)

                transaction.set(animalRef, docData)
                transaction.update(donoRef, "animais", novoAnimais)

                animalRef
            }.await()

            imageProvider.saveImage("animais/${docRef.id}", animal.imagem).await()

            return@async docRef.id
        }

    override suspend fun editarAnimal(animal: Animal): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val docData = hashMapOf(
                "nome" to animal.nome,
                "detalhes" to animal.detalhes,
            )

            db.collection("animais").document(animal.id).set(docData, SetOptions.merge()).await()
            imageProvider.saveImage("animais/${animal.id}", animal.imagem).await()
        }

    override suspend fun removerAnimal(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val animalRef = db.collection("animais").document(id)
            val usuariosRef = db.collection("usuarios")

            imageProvider.removeImage("animais/${id}").await()

            db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val idDono = animalData.data!!["dono"] as String
                val idAdotador = animalData.data!!["adotador"] as String
                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>

                // pega dados do dono
                val donoRef = usuariosRef.document(idDono)
                val donoData = transaction.get(donoRef)

                val animaisDono = donoData.data!!["animais"] as List<*>
                val novoAnimaisDono = animaisDono.filter { i -> i != id }

                // pega dados do adotador (se tiver)
                var adotadorRef: DocumentReference? = null
                var novoAdotadosAdotador: List<*>? = null

                if (idAdotador != "") {
                    adotadorRef = usuariosRef.document(idAdotador)
                    val adotadorData = transaction.get(adotadorRef)

                    val adotadosAdotador = adotadorData.data!!["adotados"] as List<*>
                    novoAdotadosAdotador = adotadosAdotador.filter { i -> i != id }
                }

                val solicitadorRefs = ArrayList<DocumentReference>()
                val novoSolicitadosList = ArrayList<List<*>>()

                for (idSolicitador in idsSolicitadores) {
                    val solicitadorRef = usuariosRef.document(idSolicitador as String)
                    val solicitadorData = transaction.get(solicitadorRef)

                    val solicitados = solicitadorData.data!!["solicitados"] as List<*>
                    val novoSolicitados = solicitados.filter { i -> i != id }

                    solicitadorRefs.add(solicitadorRef)
                    novoSolicitadosList.add(novoSolicitados)
                }

                transaction.update(donoRef, "animais", novoAnimaisDono)

                if (adotadorRef != null) {
                    transaction.update(adotadorRef, "adotados", novoAdotadosAdotador)
                }

                for (i in idsSolicitadores.indices) {
                    val solicitadorRef = solicitadorRefs[i]
                    val novoSolicitados = novoSolicitadosList[i]

                    transaction.update(solicitadorRef, "solicitados", novoSolicitados)
                }

                transaction.delete(animalRef)
            }.await()
        }

    override suspend fun animalBuscado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val animalRef = db.collection("animais").document(id)
            val usuariosRef = db.collection("usuarios")

            val success = db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val buscando = animalData.data!!["buscando"] as Boolean
                if (!buscando) {
                    return@runTransaction false
                }

                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
                val idSolicitador = idsSolicitadores[0]

                val solicitadorRef = usuariosRef.document(idSolicitador as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val solicitados = solicitadorData.data!!["solicitados"] as List<*>
                val novoSolicitados = solicitados.filter { i -> i != id }

                val adotados = solicitadorData.data!!["adotados"] as List<*>
                val novoAdotados = ArrayList(adotados)
                novoAdotados.add(id)

                val novoAnimalData = hashMapOf(
                    "adotador" to idSolicitador,
                    "solicitadores" to ArrayList<String>(),
                    "buscando" to false,
                    "detalhesAdocao" to ""
                )

                transaction.update(animalRef, novoAnimalData as Map<String, Any>)
                transaction.update(solicitadorRef, "solicitados", novoSolicitados)
                transaction.update(solicitadorRef, "adotados", novoAdotados)

                true
            }.await()

            if (!success) {
                throw Exception("Não há solicitação aceita.")
            }
        }

    override suspend fun animalEnviado(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val animalRef = db.collection("animais").document(id)
            val usuariosRef = db.collection("usuarios")

            val success = db.runTransaction { transaction ->
                val animalData = transaction.get(animalRef)

                val buscando = animalData.data!!["buscando"] as Boolean
                if (!buscando) {
                    return@runTransaction false
                }

                val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>
                val idSolicitador = idsSolicitadores[0]

                val solicitadorRef = usuariosRef.document(idSolicitador as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val solicitados = solicitadorData.data!!["solicitados"] as List<*>
                val novoSolicitados = solicitados.filter { i -> i != id }

                val adotados = solicitadorData.data!!["adotados"] as List<*>
                val novoAdotados = ArrayList(adotados)
                novoAdotados.add(id)

                val novoAnimalData = hashMapOf(
                    "adotador" to idSolicitador,
                    "solicitadores" to ArrayList<String>(),
                    "buscando" to false,
                    "detalhesAdocao" to ""
                )

                transaction.update(animalRef, novoAnimalData as Map<String, Any>)
                transaction.update(solicitadorRef, "solicitados", novoSolicitados)
                transaction.update(solicitadorRef, "adotados", novoAdotados)

                true
            }.await()

            if (!success) {
                throw Exception("Não há solicitação aceita.")
            }
        }
}
