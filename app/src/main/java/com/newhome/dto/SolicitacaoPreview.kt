package com.newhome.dto

import android.graphics.Bitmap

data class SolicitacaoPreview(
    var id: SolicitacaoID? = null,
    var titulo: String = "",
    var descricao: String = "",
    var imagemSolicitador: Bitmap? = null
) {
    companion object {
        fun fromData(data: SolicitacaoPreviewData): SolicitacaoPreview {
            return SolicitacaoPreview(
                data.id,
                data.titulo,
                data.descricao
            )
        }

        suspend fun fromData(data: SolicitacaoPreviewAsync): SolicitacaoPreview {
            return SolicitacaoPreview(
                data.id,
                data.titulo,
                data.descricao,
                data.getImagemSolicitador!!.await()
            )
        }
    }
}
