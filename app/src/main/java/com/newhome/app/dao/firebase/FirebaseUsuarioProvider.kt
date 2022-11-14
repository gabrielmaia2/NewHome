package com.newhome.app.dao.firebase

import android.graphics.Bitmap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.app.dao.IUsuarioProvider
import com.newhome.app.dao.IImageProvider
import com.newhome.app.dto.NovoUsuario
import com.newhome.app.dto.Usuario
import com.newhome.app.dto.UsuarioData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class FirebaseUsuarioProvider(
    private val db: FirebaseFirestore,
    private val imageProvider: IImageProvider
) : IUsuarioProvider {
    override suspend fun getUser(id: String): Deferred<UsuarioData> =
        CoroutineScope(Dispatchers.Main).async {
            val doc = db.collection("usuarios").document(id).get().await()

            return@async UsuarioData(
                doc.id,
                doc.data!!["nome"] as String,
                doc.data!!["detalhes"] as String
            )
        }

    override suspend fun createUser(usuario: NovoUsuario): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
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

    override suspend fun updateUser(usuario: UsuarioData): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO editar idade
            val docData = hashMapOf(
                "nome" to usuario.nome,
                "detalhes" to usuario.detalhes,
            )

            db.collection("usuarios").document(usuario.id).set(docData, SetOptions.merge()).await()
        }

    override suspend fun deleteUser(id: String): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            // TODO implementar
        }

    override suspend fun getUserImage(id: String): Deferred<Bitmap> =
        CoroutineScope(Dispatchers.Main).async {
            return@async imageProvider.getImageOrDefault("usuarios/${id}").await()
        }

    override suspend fun setUserImage(id: String, image: Bitmap?): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            return@async imageProvider.saveImage("usuarios/${id}", image).await()
        }
}
