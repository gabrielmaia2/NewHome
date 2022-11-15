package com.newhome.app.dao.firebase

import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.newhome.app.dao.IContaProvider
import com.newhome.app.dto.Credenciais
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class FirebaseContaProvider(private val auth: FirebaseAuth, private val authUI: AuthUI, private val context: Context) :
    IContaProvider {
    override fun getContaID(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun enviarEmailConfirmacao(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val currentUser = auth.currentUser ?: throw Exception("User not signed in.")
            currentUser.sendEmailVerification().await()
        }

    override fun emailConfirmacaoVerificado(): Boolean {
        val currentUser = auth.currentUser ?: throw Exception("User not signed in.")
        return currentUser.isEmailVerified
    }

    override suspend fun criarConta(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            auth.createUserWithEmailAndPassword(credenciais.email, credenciais.senha).await()
        }

    override suspend fun logar(credenciais: Credenciais): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            auth.signInWithEmailAndPassword(credenciais.email, credenciais.senha).await()
        }

    override suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            val firebaseCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(firebaseCredential).await()
        }

    override suspend fun sair(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            authUI.signOut(context).await()
        }

    override suspend fun excluirConta(): Deferred<Unit> =
        CoroutineScope(Dispatchers.Main).async {
            authUI.delete(context).await()
        }
}
