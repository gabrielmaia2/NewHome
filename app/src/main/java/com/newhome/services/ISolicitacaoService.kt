package com.newhome.services

import com.newhome.dto.*
import kotlinx.coroutines.Deferred

interface ISolicitacaoService {
    suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewAsync>>

    suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewAsync>>

    suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoAsync>

    suspend fun getStatusSolicitacao(animalId: String): Deferred<StatusSolicitacao>

    suspend fun solicitarAnimal(animalId: String): Deferred<Unit>

    suspend fun aceitarSolicitacao(
        solicitacaoId: SolicitacaoID,
        detalhesAdocao: String
    ): Deferred<Unit>

    suspend fun rejeitarSolicitacao(solicitacaoId: SolicitacaoID): Deferred<Unit>

    // adotador cancela
    suspend fun cancelarSolicitacao(animalId: String): Deferred<Unit>

    // dono cancela
    suspend fun cancelarSolicitacaoAceita(animalId: String): Deferred<Unit>
}
