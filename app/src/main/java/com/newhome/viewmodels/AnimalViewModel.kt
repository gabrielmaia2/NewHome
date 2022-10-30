package com.newhome.viewmodels

import androidx.lifecycle.ViewModel
import com.newhome.NewHomeApplication
import com.newhome.dto.Animal
import com.newhome.dto.StatusSolicitacao
import com.newhome.dto.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnimalViewModel: ViewModel() {
    private val _animalState = MutableStateFlow(Animal())
    val animalState: StateFlow<Animal> = _animalState.asStateFlow()

    private val _donoState = MutableStateFlow(Usuario())
    val donoState: StateFlow<Usuario> = _donoState.asStateFlow()

    private val _statusState = MutableStateFlow(StatusSolicitacao())
    val statusState: StateFlow<StatusSolicitacao> = _statusState.asStateFlow()

    suspend fun loadDataAsync(id: String) {
        val taskAnimal = NewHomeApplication.animalService.getAnimal(id)
        val taskDono = NewHomeApplication.animalService.getDonoInicial(id)

        val a = taskAnimal.await()
        val u = taskDono.await()

        val animalImageTask = a.getImagem!!
        val donoImageTask = u.getImagem!!
        val statusSolicitacaoTask = NewHomeApplication.solicitacaoService.getStatusSolicitacao(a.id)

        _animalState.update { Animal(a.id, a.nome, a.detalhes, animalImageTask.await()) }
        _donoState.update { Usuario(u.id, u.nome, u.detalhes, donoImageTask.await()) }
        _statusState.update { statusSolicitacaoTask.await() }
    }

    suspend fun animalBuscado() {
        NewHomeApplication.animalService.animalBuscado(animalState.value.id).await()
    }

    suspend fun cancelarSolicitacao() {
        NewHomeApplication.solicitacaoService.cancelarSolicitacao(animalState.value.id).await()
    }

    suspend fun solicitarAnimal() {
        NewHomeApplication.solicitacaoService.solicitarAnimal(animalState.value.id).await()
    }
}