package com.newhome.dao.firebase

import android.graphics.Bitmap
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.IUsuarioProvider
import com.newhome.dto.NovoUsuario
import com.newhome.dto.Usuario
import com.newhome.dto.UsuarioData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class FirebaseUsuarioProvider : IUsuarioProvider {
    private val db = Firebase.firestore

    override suspend fun getImagemUsuario(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.IO).async {
            return@async FirebaseImageProvider.instance.getImageOrDefault("usuarios/${id}").await()
        }

    override suspend fun getUsuario(id: String): Deferred<UsuarioData> =
        CoroutineScope(Dispatchers.IO).async {
            val doc = db.collection("usuarios").document(id).get().await()

            return@async UsuarioData(
                doc.id,
                doc.data!!["nome"] as String,
                doc.data!!["detalhes"] as String
            )
        }

    override suspend fun criarUsuario(usuario: NovoUsuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            val data = hashMapOf(
                "nome" to usuario.nome,
                "detalhes" to usuario.detalhes,
                "idade" to usuario.idade,
                "animais" to ArrayList<String>(),
                "adotados" to ArrayList<String>(),
                "solicitados" to ArrayList<String>()
            )

            db.collection("usuarios").document(usuario.id).set(data).await()
        }

    override suspend fun editarUsuario(usuario: Usuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            // TODO editar idade
            val docData = hashMapOf(
                "nome" to usuario.nome,
                "detalhes" to usuario.detalhes,
            )

            db.collection("usuarios").document(usuario.id).set(docData, SetOptions.merge()).await()
            FirebaseImageProvider.instance.saveImage("usuarios/${usuario.id}", usuario.imagem).await()
        }

    override suspend fun deleteUsuario(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.IO).async {
            // TODO implementar
        }
}
