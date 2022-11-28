package com.newhome.app.viewmodels

import androidx.lifecycle.ViewModel
import com.newhome.app.NewHomeApplication
import com.newhome.app.dto.Solicitacao
import com.newhome.app.dto.SolicitacaoID
import com.newhome.app.dto.StatusSolicitacao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SolicitacaoViewModel : ViewModel() {
    private val _solicitacaoState = MutableStateFlow(Solicitacao())
    val solicitacaoState: StateFlow<Solicitacao> = _solicitacaoState.asStateFlow()

    private val _statusState = MutableStateFlow(StatusSolicitacao())
    val statusState: StateFlow<StatusSolicitacao> = _statusState.asStateFlow()

    suspend fun loadDataAsync(id: SolicitacaoID) {
        val solicitacaoTask = NewHomeApplication.solicitacaoService.getSolicitacao(id)
        val animalImageTask = NewHomeApplication.animalService.getImagemAnimal(id.animalID)
        val solicitadorImageTask =
            NewHomeApplication.usuarioService.getImagemUsuario(id.solicitadorID)
        val statusSolicitacaoTask =
            NewHomeApplication.solicitacaoService.getStatusSolicitacao(id.animalID)

        val solicitacao = Solicitacao.fromData(solicitacaoTask.await())
        solicitacao.animal!!.image = animalImageTask.await()
        solicitacao.solicitador!!.image = solicitadorImageTask.await()
        _solicitacaoState.update { solicitacao }

        val status = statusSolicitacaoTask.await()
        _statusState.update { status }
    }

    suspend fun aceitarSolicitacao(detalhes: String) {
        NewHomeApplication.solicitacaoService.aceitarSolicitacao(
            solicitacaoState.value.id!!,
            detalhes
        ).await()
    }

    suspend fun rejeitarSolicitacao() {
        NewHomeApplication.solicitacaoService.rejeitarSolicitacao(solicitacaoState.value.id!!).await()
    }

    suspend fun animalBuscado() {
        NewHomeApplication.animalService.animalBuscado(solicitacaoState.value.animal!!.id).await()
    }

    suspend fun cancelarSolicitacao() {
        NewHomeApplication.solicitacaoService.cancelarSolicitacaoAceita(solicitacaoState.value.animal!!.id).await()
    }
}
