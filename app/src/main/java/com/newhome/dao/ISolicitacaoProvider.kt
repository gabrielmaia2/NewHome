package com.newhome.dao

import com.newhome.dto.*
import kotlinx.coroutines.Deferred

interface ISolicitacaoProvider {
    suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewData>>

    suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewData>>

    suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoData>

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
