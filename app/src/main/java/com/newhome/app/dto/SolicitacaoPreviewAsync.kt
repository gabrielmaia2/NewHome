package com.newhome.app.dto

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

class SolicitacaoPreviewAsync(
    var id: SolicitacaoID? = null,
    var titulo: String = "",
    var descricao: String = "",
    var getImagemSolicitador: Deferred<Bitmap>? = null
) {
    companion object {
        fun fromData(data: SolicitacaoPreviewData): SolicitacaoPreviewAsync {
            return SolicitacaoPreviewAsync(
                data.id,
                data.titulo,
                data.descricao
            )
        }
    }
}
