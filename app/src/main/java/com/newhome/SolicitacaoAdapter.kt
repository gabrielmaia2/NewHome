package com.newhome

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.newhome.dto.SolicitacaoPreview

class SolicitacaoAdapter(
    context: Context,
    var solicitacoes: List<SolicitacaoPreview> = ArrayList()
) :
    ArrayAdapter<SolicitacaoPreview>(context, R.layout.fragment_solicitacao_preview, solicitacoes) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.fragment_solicitacao_preview, parent, false)

        val solicitacao = solicitacoes[position]

        val solicitadorImagem: ImageView = v.findViewById(R.id.solicitadorImagem)
        val tituloSolicitacao: TextView = v.findViewById(R.id.tituloSolicitacao)
        val descricaoSolicitacao: TextView = v.findViewById(R.id.descricaoSolicitacao)

        tituloSolicitacao.text = solicitacao.titulo
        descricaoSolicitacao.text = solicitacao.descricao
        solicitadorImagem.setImageBitmap(solicitacao.imagemSolicitador)

        return v
    }
}
