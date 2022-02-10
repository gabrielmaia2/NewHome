package com.newhome.dao.firebase

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.IUsuarioProvider
import com.newhome.dao.ImagemProvider
import com.newhome.dto.Usuario

class FirebaseUsuarioProvider(val context: Context) : IUsuarioProvider {
    private val db = Firebase.firestore

    override fun getUsuarioAtual(
        onSuccess: (usuario: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        val usuarioAtual = FirebaseAuth.getInstance().currentUser!!

        db.collection("usuarios").document(usuarioAtual.uid)
            .get()
            .addOnSuccessListener { doc ->
                val usuario = Usuario()
                usuario.id = doc.id
                usuario.nome = doc.data!!["nome"] as String
                usuario.detalhes = doc.data!!["detalhes"] as String

                val onLoadBitmap = { bitmap: Bitmap ->
                    usuario.imagem = bitmap
                    onSuccess(usuario)
                }

                ImagemProvider.getImageFromFirebase("usuarios/${usuario.id}", { bitmap ->
                    onLoadBitmap(bitmap)
                }, {
                    onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun getUsuario(
        id: String,
        onSuccess: (usuario: Usuario) -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        db.collection("usuarios").document(id)
            .get()
            .addOnSuccessListener { doc ->
                val usuario = Usuario()
                usuario.id = doc.id
                usuario.nome = doc.data!!["nome"] as String
                usuario.detalhes = doc.data!!["detalhes"] as String

                val onLoadBitmap = { bitmap: Bitmap ->
                    usuario.imagem = bitmap
                    onSuccess(usuario)
                }

                ImagemProvider.getImageFromFirebase("usuarios/${usuario.id}", { bitmap ->
                    onLoadBitmap(bitmap)
                }, {
                    onLoadBitmap(ImagemProvider.getDefaultBitmap(context))
                })
            }
            .addOnFailureListener(onFailure)
    }

    override fun editarUsuarioAtual(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        // TODO editar idade
        val docData = hashMapOf(
            "nome" to usuario.nome,
            "detalhes" to usuario.detalhes,
        )

        db.collection("usuarios").document(usuario.id)
            .set(docData, SetOptions.merge())
            .addOnSuccessListener {
                ImagemProvider.saveImageToFirebase(
                    "usuarios/${usuario.id}",
                    usuario.imagem,
                    onSuccess,
                    onFailure
                )
            }
            .addOnFailureListener(onFailure)
    }
}
