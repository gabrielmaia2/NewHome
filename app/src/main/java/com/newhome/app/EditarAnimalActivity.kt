package com.newhome.app

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.dto.Animal
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import com.newhome.app.utils.PictureTaker
import kotlinx.coroutines.launch

class EditarAnimalActivity : AppCompatActivity() {
    private lateinit var animalEditImage: ImageView

    private lateinit var nomeAnimalEditText: EditText

    private lateinit var descricaoAnimalEditText: EditText
    private lateinit var editarMapaAnimalButton: Button

    private lateinit var concluirEditarAnimalButton: Button
    private lateinit var cancelarEditarAnimalButton: Button

    private lateinit var animal: Animal

    private lateinit var pictureTaker: PictureTaker

    private val loadingDialog = LoadingDialog(this)
    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        animalEditImage = findViewById(R.id.animalEditImage)

        nomeAnimalEditText = findViewById(R.id.nomeAnimalEditText)
        descricaoAnimalEditText = findViewById(R.id.descricaoAnimalEditText)

        editarMapaAnimalButton = findViewById(R.id.editarMapaAnimalButton)
        concluirEditarAnimalButton = findViewById(R.id.concluirEditarAnimalButton)
        cancelarEditarAnimalButton = findViewById(R.id.cancelarEditarAnimalButton)

        // TODO setar mapa listener e retornar nova posicao do mapa quando terminar de editar

        createPictureTaker()
        lifecycleScope.launch { carregarDados() }
        animalEditImage.setOnClickListener { pictureTaker.takePicture() }
        concluirEditarAnimalButton.setOnClickListener { lifecycleScope.launch { onConcluir() } }
        cancelarEditarAnimalButton.setOnClickListener { onCancelar() }
    }

    override fun onPause() {
        super.onPause()
        loadingDialog.stop()
        dialog.stop()
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

    private suspend fun carregarDados() {
        // carrega os dados do database e preenche os campos

        val id = intent.getStringExtra("id")!!

        loadingDialog.start()

        try {
            val a = NewHomeApplication.animalService.getAnimal(id).await()
            animal = if (a != null) Animal.fromData(a) else Animal()
            nomeAnimalEditText.setText(animal.name)
            descricaoAnimalEditText.setText(animal.details)

            animalEditImage.setImageBitmap(animal.image)
        } catch (e: Exception) {
            errorLoading(e)
            return
        }

        loadingDialog.stop()
    }

    private fun errorLoading(e: Exception) {
        dialogDisplayer.display("Falha ao carregar animal", e)
        finish()
        loadingDialog.stop()
    }

    private fun createPictureTaker() {
        pictureTaker = PictureTaker(this, { bitmap ->
            animal.image = bitmap
            animalEditImage.setImageBitmap(bitmap)
        }, { e ->
            dialogDisplayer.display("Falha ao acessar câmera", e)
        })
    }

    private suspend fun onConcluir() {
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal editado

        animal.name = nomeAnimalEditText.text.toString()
        animal.details = descricaoAnimalEditText.text.toString()
        // TODO enviar posicao no mapa

        dialog.start()

        try {
            NewHomeApplication.animalService.editarAnimal(animal).await()
        } catch (e: Exception) {
            dialogDisplayer.display("Falha ao editar animal", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Animal editado com sucesso")
        finish()
        dialog.stop()
    }

    private fun onCancelar() {
        // volta pra tela de animal dono sem editar nada
        finish()
    }
}
