package com.newhome.app.services

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.newhome.app.dto.Credentials
import com.newhome.app.dto.NewAccount
import kotlinx.coroutines.Deferred

interface IContaService {
    fun getContaID(): String?

    suspend fun enviarEmailConfirmacao(): Deferred<Unit>

    suspend fun cadastrar(newAccount: NewAccount): Deferred<Unit>

    suspend fun logar(credentials: Credentials): Deferred<Unit>

    suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit>

    fun tentarUsarContaLogada()

    suspend fun sair(): Deferred<Unit>

    suspend fun excluirConta(): Deferred<Unit>
}
