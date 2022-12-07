package com.newhome.app.viewmodels

import androidx.lifecycle.ViewModel
import com.newhome.app.NewHomeApplication
import com.newhome.app.dto.Animal
import com.newhome.app.dto.StatusSolicitacao
import com.newhome.app.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnimalViewModel : ViewModel() {
    private val _animalState = MutableStateFlow(Animal())
    val animalState: StateFlow<Animal> = _animalState.asStateFlow()

    private val _donoState = MutableStateFlow(User())
    val donoState: StateFlow<User> = _donoState.asStateFlow()

    private val _statusState = MutableStateFlow(StatusSolicitacao())
    val statusState: StateFlow<StatusSolicitacao> = _statusState.asStateFlow()

    suspend fun loadDataAsync(id: String) {
        val taskAnimal = NewHomeApplication.animalService.getAnimal(id)
        val taskDono = NewHomeApplication.animalService.getDonoInicial(id)

        val a = taskAnimal.await()
        val u = taskDono.await()

        val animalImageTask = a?.getImage
        val donoImageTask = u?.getImage
        val statusSolicitacaoTask =
            if (a == null) null else NewHomeApplication.solicitacaoService.getStatusSolicitacao(a.id)

        _animalState.update {
            Animal(
                a?.id ?: "0",
                a?.name ?: "null",
                a?.details ?: "null",
                animalImageTask?.await()
            )
        }
        _donoState.update {
            User(
                u?.id ?: "0",
                u?.name ?: "null",
                u?.details ?: "null",
                donoImageTask?.await()
            )
        }
        _statusState.update { statusSolicitacaoTask?.await() ?: StatusSolicitacao() }
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