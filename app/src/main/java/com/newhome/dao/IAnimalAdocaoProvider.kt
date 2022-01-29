package com.newhome.dao

interface IPorAnimalEmAdocaoProvider {
    fun porAnimalEmAdocao()
    fun cancelarPorEmAdocao()
    fun marcarAnimalBuscado()
    fun aceitarSolicitacaoAdocao()
    fun rejeitarSolicitacaoAdocao()
}