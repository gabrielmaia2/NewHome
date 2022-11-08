package com.newhome.dao

import com.newhome.dto.Credenciais
import kotlinx.coroutines.Deferred

interface IContaProvider {
    fun getContaID(): String?

    suspend fun enviarEmailConfirmacao(): Deferred<Unit>

    fun emailConfirmacaoVerificado(): Boolean

    suspend fun criarConta(credenciais: Credenciais): Deferred<Unit>

    suspend fun logar(credenciais: Credenciais) : Deferred<Unit>

    suspend fun sair() : Deferred<Unit>

    suspend fun excluirConta() : Deferred<Unit>
}
