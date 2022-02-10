package com.newhome.dao.firebase

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.IAnimalProvider
import com.newhome.dao.ImagemProvider
import com.newhome.dto.Animal
import com.newhome.dto.Usuario

class FirebaseAnimalProvider(val context: Context) : IAnimalProvider {
    private val db = Firebase.firestore

    override fun getTodosAnimais(
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        db.collection("animais")
            .get()
            .addOnSuccessListener { docs ->
                val animais = ArrayList<Animal>()
                for (doc in docs) {
                    val animal = Animal()
                    animal.id = doc.id
                    animal.nome = doc.data["nome"] as String
                    animal.detalhes = doc.data["detalhes"] as String

                    val onLoadBitmap = { bitmap: Bitmap ->
                        animal.imagem = bitmap
                        onSuccess(animais)
                    }

                    ImagemProvider.getImageFromFirebase("animais/${animal.id}", { bitmap ->
                        onLoadBitmap(bitmap)
                    }, {
                        onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                    })

                    animais.add(animal)
                }
                onSuccess(animais)
            }
            .addOnFailureListener(onFailure)
    }

    override fun getAnimaisPostosAdocao(
        donoId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docRef = db.collection("usuarios").document(donoId)
        val animaisRef = db.collection("animais")

        db.runTransaction { transaction ->
            val animais = ArrayList<Animal>()

            val snapshot = transaction.get(docRef)
            val idsAnimais = snapshot.data!!["animais"] as List<*>

            for (id in idsAnimais) {
                val animalRef = animaisRef.document(id as String)
                val animalData = transaction.get(animalRef)

                val animal = Animal()
                animal.id = animalData.id
                animal.nome = animalData.data!!["nome"] as String
                animal.detalhes = animalData.data!!["detalhes"] as String

                animais.add(animal)
            }

            animais
        }
            .addOnSuccessListener { animais ->
                onSuccess(animais)

                for (animal in animais) {
                    val onLoadBitmap = { bitmap: Bitmap ->
                        animal.imagem = bitmap
                        onSuccess(animais)
                    }

                    ImagemProvider.getImageFromFirebase("animais/${animal.id}", { bitmap ->
                        onLoadBitmap(bitmap)
                    }, {
                        onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                    })
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun getAnimaisAdotados(
        adotadorId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docRef = db.collection("usuarios").document(adotadorId)
        val animaisRef = db.collection("animais")

        db.runTransaction { transaction ->
            val animais = ArrayList<Animal>()

            val snapshot = transaction.get(docRef)
            val idsAnimais = snapshot.data!!["adotados"] as List<*>

            for (id in idsAnimais) {
                val animalRef = animaisRef.document(id as String)
                val animalData = transaction.get(animalRef)

                val animal = Animal()
                animal.id = animalData.id
                animal.nome = animalData.data!!["nome"] as String
                animal.detalhes = animalData.data!!["detalhes"] as String

                animais.add(animal)
            }

            animais
        }
            .addOnSuccessListener { animais ->
                onSuccess(animais)

                for (animal in animais) {
                    val onLoadBitmap = { bitmap: Bitmap ->
                        animal.imagem = bitmap
                        onSuccess(animais)
                    }

                    ImagemProvider.getImageFromFirebase("animais/${animal.id}", { bitmap ->
                        onLoadBitmap(bitmap)
                    }, {
                        onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                    })
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun getAnimaisSolicitados(
        solicitadorId: String,
        onSuccess: (animais: List<Animal>) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docRef = db.collection("usuarios").document(solicitadorId)
        val animaisRef = db.collection("animais")

        db.runTransaction { transaction ->
            val animais = ArrayList<Animal>()

            val snapshot = transaction.get(docRef)
            val idsAnimais = snapshot.data!!["solicitados"] as List<*>

            for (id in idsAnimais) {
                val animalRef = animaisRef.document(id as String)
                val animalData = transaction.get(animalRef)

                val animal = Animal()
                animal.id = animalData.id
                animal.nome = animalData.data!!["nome"] as String
                animal.detalhes = animalData.data!!["detalhes"] as String

                animais.add(animal)
            }

            animais
        }
            .addOnSuccessListener { animais ->
                onSuccess(animais)

                for (animal in animais) {
                    val onLoadBitmap = { bitmap: Bitmap ->
                        animal.imagem = bitmap
                        onSuccess(animais)
                    }

                    ImagemProvider.getImageFromFirebase("animais/${animal.id}", { bitmap ->
                        onLoadBitmap(bitmap)
                    }, {
                        onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                    })
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun getDonoInicial(
        animalId: String,
        onSuccess: (dono: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docRef = db.collection("animais").document(animalId)
        val usuariosRef = db.collection("usuarios")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val donoId = snapshot.getString("dono")!!

            val donoRef = usuariosRef.document(donoId)
            val donoData = transaction.get(donoRef)

            val dono = Usuario()
            dono.id = donoData.id
            dono.nome = donoData.data!!["nome"] as String
            dono.detalhes = donoData.data!!["detalhes"] as String

            dono
        }
            .addOnSuccessListener { dono ->
                val onLoadBitmap = { bitmap: Bitmap ->
                    dono.imagem = bitmap
                    onSuccess(dono)
                }

                ImagemProvider.getImageFromFirebase("usuarios/${dono.id}", { bitmap ->
                    onLoadBitmap(bitmap)
                }, {
                    onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun getAdotador(
        animalId: String,
        onSuccess: (adotador: Usuario?) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docRef = db.collection("animais").document(animalId)
        val usuariosRef = db.collection("usuarios")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val adotadorId = snapshot.getString("adotador")!!

            if (adotadorId == "") {
                return@runTransaction null
            }

            val adotadorRef = usuariosRef.document(adotadorId)
            val adotadorData = transaction.get(adotadorRef)

            val dono = Usuario()
            dono.id = adotadorData.id
            dono.nome = adotadorData.data!!["nome"] as String
            dono.detalhes = adotadorData.data!!["detalhes"] as String

            dono
        }
            .addOnSuccessListener { adotador: Usuario? ->
                if (adotador == null) {
                    onSuccess(adotador)
                    return@addOnSuccessListener
                }

                val onLoadBitmap = { bitmap: Bitmap ->
                    adotador.imagem = bitmap
                    onSuccess(adotador)
                }

                ImagemProvider.getImageFromFirebase("usuarios/${adotador.id}", { bitmap ->
                    onLoadBitmap(bitmap)
                }, {
                    onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun getAnimal(
        id: String,
        onSuccess: (animal: Animal) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        db.collection("animais").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val animal = Animal()
                animal.id = doc.id
                animal.nome = doc.data!!["nome"] as String
                animal.detalhes = doc.data!!["detalhes"] as String

                val onLoadBitmap = { bitmap: Bitmap ->
                    animal.imagem = bitmap
                    onSuccess(animal)
                }

                ImagemProvider.getImageFromFirebase("animais/${animal.id}", { bitmap ->
                    onLoadBitmap(bitmap)
                }, {
                    onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun adicionarAnimal(
        animal: Animal,
        onSuccess: (id: String) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser!!

        val animalRef = db.collection("animais").document()
        val donoRef = db.collection("usuarios").document(usuario.uid)

        db.runTransaction { transaction ->
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
        }
            .addOnSuccessListener { docRef ->
                ImagemProvider.saveImageToFirebase("animais/${docRef.id}", animal.imagem, {
                    onSuccess(docRef.id)
                }, onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    override fun editarAnimal(
        animal: Animal,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val docData = hashMapOf(
            "nome" to animal.nome,
            "detalhes" to animal.detalhes,
        )

        db.collection("animais").document(animal.id)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                ImagemProvider.saveImageToFirebase("animais/${animal.id}", animal.imagem, {
                    onSuccess()
                }, onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    override fun removerAnimal(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val animalRef = db.collection("animais").document(id)
        val usuariosRef = db.collection("usuarios")

        db.runTransaction { transaction ->
            val animalData = transaction.get(animalRef)

            val idDono = animalData.data!!["dono"] as String
            val idAdotador = animalData.data!!["adotador"] as String
            val idsSolicitadores = animalData.data!!["solicitadores"] as List<*>

            // pega dados do dono
            val donoRef = usuariosRef.document(idDono)
            val donoData = transaction.get(donoRef)

            val animaisDono = donoData.data!!["animais"] as List<*>
            val novoAnimaisDono = animaisDono.filter { i -> i == id }

            // pega dados do adotador (se tiver)
            var adotadorRef: DocumentReference? = null
            var novoAdotadosAdotador: List<*>? = null

            if (idAdotador != "") {
                adotadorRef = usuariosRef.document(idAdotador)
                val adotadorData = transaction.get(adotadorRef)

                val adotadosAdotador = adotadorData.data!!["adotados"] as List<*>
                novoAdotadosAdotador = adotadosAdotador.filter { i -> i == id }
            }

            val solicitadorRefs = ArrayList<DocumentReference>()
            val novoSolicitadosList = ArrayList<List<*>>()

            for (idSolicitador in idsSolicitadores) {
                val solicitadorRef = usuariosRef.document(idSolicitador as String)
                val solicitadorData = transaction.get(solicitadorRef)

                val solicitados = solicitadorData.data!!["solicitados"] as List<*>
                val novoSolicitados = solicitados.filter { i -> i == id }

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
        }
            .addOnSuccessListener {
                ImagemProvider.removeImageFromFirebase("animais/${id}", onSuccess, onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    override fun animalBuscado(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val animalRef = db.collection("animais").document(id)
        val usuariosRef = db.collection("usuarios")

        db.runTransaction { transaction ->
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
        }
            .addOnSuccessListener { success ->
                if (success){
                    onSuccess()
                }
                else {
                    onFailure(Exception("Não há solicitação aceita."))
                }
            }
            .addOnFailureListener(onFailure)
    }

    override fun animalEnviado(
        id: String,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val animalRef = db.collection("animais").document(id)
        val usuariosRef = db.collection("usuarios")

        db.runTransaction { transaction ->
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
        }
            .addOnSuccessListener { success ->
                if (success){
                    onSuccess()
                }
                else {
                    onFailure(Exception("Não há solicitação aceita."))
                }
            }
            .addOnFailureListener(onFailure)
    }
}
