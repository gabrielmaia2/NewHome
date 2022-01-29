package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.newhome.dto.Animal

class EditarAnimalActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalEditImage: ImageView

    private lateinit var nomeAnimalEditText: EditText
    private lateinit var descricaoAnimalEditText: EditText

    private lateinit var editarMapaAnimalButton: Button
    private lateinit var concluirEditarAnimalButton: Button
    private lateinit var cancelarEditarAnimalButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar animal"

        animal = intent.getSerializableExtra("animal") as Animal

        animalEditImage = findViewById(R.id.animalEditImage)

        nomeAnimalEditText = findViewById(R.id.nomeAnimalEditText)
        descricaoAnimalEditText = findViewById(R.id.descricaoAnimalEditText)

        editarMapaAnimalButton = findViewById(R.id.editarMapaAnimalButton)
        concluirEditarAnimalButton = findViewById(R.id.concluirEditarAnimalButton)
        cancelarEditarAnimalButton = findViewById(R.id.cancelarEditarAnimalButton)

        // TODO setar mapa listener e retornar nova posicao do mapa quando terminar de editar

        preencherCampos()
        concluirEditarAnimalButton.setOnClickListener { onConcluir() }
        cancelarEditarAnimalButton.setOnClickListener { onCancelar() }
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

    private fun preencherCampos() {
        // preenche os campos do animal na tela

        nomeAnimalEditText.setText(animal.nome)
        descricaoAnimalEditText.setText(animal.detalhes)
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            animalEditImage.setImageDrawable(drawable)
        }
    }

    private fun onConcluir() {
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal editado

        // TODO enviar imagem pro database e colocar url aqui
        animal.imagemURL = ""
        animal.nome = nomeAnimalEditText.text.toString()
        animal.detalhes = descricaoAnimalEditText.text.toString()
        // TODO enviar posicao no mapa

        // TODO enviar animal pro database

        val intent = Intent(applicationContext, AnimalDonoActivity::class.java)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onCancelar() {
        // volta pra tela de animal dono sem editar nada

        val intent = Intent(applicationContext, AnimalDonoActivity::class.java)
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}