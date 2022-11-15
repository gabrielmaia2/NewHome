package com.newhome.app.services

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.newhome.app.dto.Credenciais
import com.newhome.app.dto.NovaConta
import kotlinx.coroutines.Deferred

interface IContaService {
    fun getContaID(): String?

    suspend fun enviarEmailConfirmacao(): Deferred<Unit>

    suspend fun cadastrar(novaConta: NovaConta): Deferred<Unit>

    suspend fun logar(credenciais: Credenciais): Deferred<Unit>

    suspend fun entrarComGoogle(account: GoogleSignInAccount): Deferred<Unit>

    fun tentarUsarContaLogada()

    suspend fun sair(): Deferred<Unit>

    suspend fun excluirConta(): Deferred<Unit>
}
