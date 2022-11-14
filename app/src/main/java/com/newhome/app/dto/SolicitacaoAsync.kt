package com.newhome.app.dto

class SolicitacaoAsync(
    var id: SolicitacaoID? = null,
    var animal: AnimalAsync? = null,
    var solicitador: UsuarioAsync? = null
) {
    companion object {
        fun fromData(data: SolicitacaoData): SolicitacaoAsync {
            return SolicitacaoAsync(
                data.id,
                AnimalAsync.fromData(data.animal!!),
                UsuarioAsync.fromData(data.solicitador!!)
            )
        }
    }
}
