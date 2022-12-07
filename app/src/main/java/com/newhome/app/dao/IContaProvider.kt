package com.newhome.app.dao

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.newhome.app.dto.Credentials
import kotlinx.coroutines.Deferred

interface IContaProvider {
    fun getContaID(): String?

    suspend fun enviarEmailConfirmacao(): Deferred<Unit>

    fun emailConfirmacaoVerificado(): Boolean

    suspend fun criarConta(credentials: Credentials): Deferred<Unit>

    suspend fun logar(credentials: Credentials) : Deferred<Unit>

    suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit>

    suspend fun sair() : Deferred<Unit>

    suspend fun excluirConta() : Deferred<Unit>
}
