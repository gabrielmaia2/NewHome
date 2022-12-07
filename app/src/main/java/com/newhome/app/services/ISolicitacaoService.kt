package com.newhome.app.services

import com.newhome.app.dto.SolicitacaoAsync
import com.newhome.app.dto.SolicitacaoID
import com.newhome.app.dto.SolicitacaoPreviewAsync
import com.newhome.app.dto.StatusSolicitacao
import com.newhome.app.dto.*
import kotlinx.coroutines.Deferred

interface ISolicitacaoService {
    suspend fun getTodasSolicitacoes(): Deferred<List<SolicitacaoPreviewAsync>>

    suspend fun getTodasSolicitacoesAnimal(animalId: String): Deferred<List<SolicitacaoPreviewAsync>?>

    suspend fun getSolicitacao(solicitacaoId: SolicitacaoID): Deferred<SolicitacaoAsync>

    suspend fun getStatusSolicitacao(animalId: String): Deferred<StatusSolicitacao?>

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
