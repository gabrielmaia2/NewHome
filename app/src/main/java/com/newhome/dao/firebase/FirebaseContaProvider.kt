package com.newhome.dao.firebase

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.newhome.dao.IContaProvider
import com.newhome.dto.Credenciais
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class FirebaseContaProvider(private val context: Context) : IContaProvider {
    override fun getContaID(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun enviarEmailConfirmacao(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification().await()
        }

    override fun emailConfirmacaoVerificado(): Boolean {
        return FirebaseAuth.getInstance().currentUser!!.isEmailVerified
    }

    override suspend fun criarConta(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(credenciais.email, credenciais.senha)
                .await()
        }

    override suspend fun logar(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(credenciais.email, credenciais.senha).await()
        }

    override suspend fun sair(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            AuthUI.getInstance().signOut(context).await()
        }

    override suspend fun excluirConta(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            AuthUI.getInstance().delete(context).await()
        }
}
