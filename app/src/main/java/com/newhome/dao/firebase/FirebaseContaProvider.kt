package com.newhome.dao.firebase

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.newhome.dao.IContaProvider
import com.newhome.dto.ContaLogin
import com.newhome.dto.NovaConta

class FirebaseContaProvider(val context: Context) : IContaProvider {
    private lateinit var usuario: FirebaseUser
    private val db = Firebase.firestore

    override fun cadastrar(
        novaConta: NovaConta,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(novaConta.email, novaConta.senha)
            .addOnSuccessListener {
                usuario = FirebaseAuth.getInstance().currentUser!!

                val data = hashMapOf(
                    "nome" to novaConta.nome,
                    "detalhes" to "",
                    "idade" to novaConta.idade,
                    "animais" to ArrayList<String>(),
                    "adotados" to ArrayList<String>(),
                    "solicitados" to ArrayList<String>()
                )
                db.collection("usuarios").document(usuario.uid)
                    .set(data)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    override fun logar(
        contaLogin: ContaLogin,
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(contaLogin.email, contaLogin.senha)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    override fun tentarUsarContaLogada(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        // TODO implementar
        val e = Exception("TODO implement.")
        onFailure(e)
    }

    override fun sair(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        AuthUI.getInstance().signOut(context)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    override fun excluirConta(
        onSuccess: () -> Unit,
        onFailure: (e: Exception) -> Unit
    ) {
        // TODO implementar
        AuthUI.getInstance().delete(context)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}
