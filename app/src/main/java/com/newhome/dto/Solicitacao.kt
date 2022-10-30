package com.newhome.dto

data class Solicitacao(
    var id: SolicitacaoID? = null,
    var animal: Animal? = null,
    var solicitador: Usuario? = null
) {
    companion object {
        fun fromData(data: SolicitacaoData): Solicitacao {
            return Solicitacao(
                data.id,
                Animal.fromData(data.animal!!),
                Usuario.fromData(data.solicitador!!)
            )
        }

        suspend fun fromData(data: SolicitacaoAsync): Solicitacao {
            return Solicitacao(
                data.id,
                Animal.fromData(data.animal!!),
                Usuario.fromData(data.solicitador!!)
            )
        }
    }
}
