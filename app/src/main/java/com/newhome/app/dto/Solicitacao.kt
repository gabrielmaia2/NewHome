package com.newhome.app.dto

data class Solicitacao(
    var id: SolicitacaoID? = null,
    var animal: Animal? = null,
    var solicitador: User? = null
) {
    companion object {
        fun fromData(data: SolicitacaoData): Solicitacao {
            return Solicitacao(
                data.id,
                Animal.fromData(data.animal!!),
                User.fromData(data.solicitador!!)
            )
        }

        suspend fun fromData(data: SolicitacaoAsync): Solicitacao {
            return Solicitacao(
                data.id,
                Animal.fromData(data.animal!!),
                User.fromData(data.solicitador!!)
            )
        }
    }
}
