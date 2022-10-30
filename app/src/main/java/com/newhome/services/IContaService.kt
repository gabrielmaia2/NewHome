package com.newhome.services

import com.newhome.dto.Credenciais
import com.newhome.dto.NovaConta
import kotlinx.coroutines.Deferred

interface IContaService {
    fun getContaID(): String?

    suspend fun cadastrar(novaConta: NovaConta): Deferred<Unit>

    suspend fun logar(credenciais: Credenciais): Deferred<Unit>

    fun tentarUsarContaLogada()

    suspend fun sair(): Deferred<Unit>

    suspend fun excluirConta(): Deferred<Unit>
}
