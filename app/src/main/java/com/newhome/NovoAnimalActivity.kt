package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.newhome.dto.Animal

class NovoAnimalActivity : AppCompatActivity() {
    private lateinit var adicionarAnimalImage: ImageView
    private lateinit var adicionarAnimalEditIcon: ImageView

    private lateinit var nomeNovoAnimalText: EditText
    private lateinit var descricaoNovoAnimalText: EditText

    private lateinit var localMapaButton: Button
    private lateinit var concluirAdicionarAnimalButton: Button
    private lateinit var cancelarAdicionarAnimalButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar animal"

        adicionarAnimalImage = findViewById(R.id.adicionarAnimalImage)
        adicionarAnimalEditIcon = findViewById(R.id.adicionarAnimalEditIcon)

        nomeNovoAnimalText = findViewById(R.id.nomeNovoAnimalText)
        descricaoNovoAnimalText = findViewById(R.id.descricaoNovoAnimalText)

        localMapaButton = findViewById(R.id.localMapaButton)
        concluirAdicionarAnimalButton = findViewById(R.id.concluirAdicionarAnimalButton)
        cancelarAdicionarAnimalButton = findViewById(R.id.cancelarAdicionarAnimalButton)

        // TODO setar localMapaButton
        // TODO trocar o texto do mapa de colocar pra editar quando colocar localizacao
        concluirAdicionarAnimalButton.setOnClickListener { onConcluir() }
        cancelarAdicionarAnimalButton.setOnClickListener { onCancelar() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onCancelar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onConcluir() {
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal editado

        // TODO enviar imagem pro database e colocar url aqui
        val animal = Animal()
        animal.imagemURL = ""
        animal.nome = nomeNovoAnimalText.text.toString()
        animal.detalhes = descricaoNovoAnimalText.text.toString()
        // TODO enviar posicao no mapa

        // TODO enviar animal pro database

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onCancelar() {
        // volta pra tela de lista sem adicionar animal

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}