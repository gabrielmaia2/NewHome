package com.newhome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class SolicitacaoDetalhesActivity : AppCompatActivity() {
    private lateinit var solicitacaoDetalhesDetalhesText: TextView

    private lateinit var solicitacaoDetalhesConcluirButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitacao_detalhes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes"

        solicitacaoDetalhesDetalhesText = findViewById(R.id.solicitacaoDetalhesDetalhesText)

        solicitacaoDetalhesConcluirButton = findViewById(R.id.solicitacaoDetalhesConcluirButton)

        solicitacaoDetalhesConcluirButton.setOnClickListener { finish() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}