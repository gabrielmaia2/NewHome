package com.newhome.app

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.newhome.app.R
import com.newhome.app.dto.Animal
import com.newhome.app.utils.DialogDisplayer
import com.newhome.app.utils.LoadingDialog
import com.newhome.app.utils.PictureTaker
import kotlinx.coroutines.launch

class NovoAnimalActivity : AppCompatActivity() {
    private lateinit var adicionarAnimalImage: ImageView
    private lateinit var adicionarAnimalEditIcon: ImageView

    private lateinit var nomeNovoAnimalText: EditText
    private lateinit var descricaoNovoAnimalText: EditText

    private lateinit var localMapaButton: Button
    private lateinit var concluirAdicionarAnimalButton: Button
    private lateinit var cancelarAdicionarAnimalButton: Button

    private var imagem: Bitmap? = null

    private lateinit var pictureTaker: PictureTaker

    private val dialog = LoadingDialog(this)
    private lateinit var dialogDisplayer: DialogDisplayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar animal"

        dialogDisplayer = DialogDisplayer(applicationContext)

        adicionarAnimalImage = findViewById(R.id.adicionarAnimalImage)
        adicionarAnimalEditIcon = findViewById(R.id.adicionarAnimalEditIcon)

        nomeNovoAnimalText = findViewById(R.id.nomeNovoAnimalText)
        descricaoNovoAnimalText = findViewById(R.id.descricaoNovoAnimalText)

        localMapaButton = findViewById(R.id.localMapaButton)
        concluirAdicionarAnimalButton = findViewById(R.id.concluirAdicionarAnimalButton)
        cancelarAdicionarAnimalButton = findViewById(R.id.cancelarAdicionarAnimalButton)

        // TODO setar localMapaButton
        // TODO trocar o texto do mapa de colocar pra editar quando colocar localizacao

        createPictureTaker()
        adicionarAnimalImage.setOnClickListener { pictureTaker.takePicture() }
        concluirAdicionarAnimalButton.setOnClickListener { lifecycleScope.launch { onConcluir() } }
        cancelarAdicionarAnimalButton.setOnClickListener { onCancelar() }
    }

    override fun onPause() {
        super.onPause()
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

    private fun createPictureTaker() {
        pictureTaker = PictureTaker(this, { bitmap ->
            imagem = bitmap
            adicionarAnimalImage.setImageBitmap(bitmap)
        }, { e ->
            dialogDisplayer.display("Falha ao acessar câmera", e)
            finish()
        })
    }

    private suspend fun onConcluir() {
        // conclui a edicao e retorna para a tela de animal dono enviando o novo animal criado

        dialog.start()

        val animal = Animal(
            "",
            nomeNovoAnimalText.text.toString(),
            descricaoNovoAnimalText.text.toString(),
            imagem
        )
        // TODO enviar posicao no mapa

        try {
            NewHomeApplication.animalService.adicionarAnimal(animal).await()
        } catch (e:Exception) {
            dialogDisplayer.display("Falha ao adicionar animal", e)
            dialog.stop()
            return
        }

        dialogDisplayer.display("Animal adicionado com sucesso")
        finish()
        dialog.stop()
    }

    private fun onCancelar() {
        // volta pra tela de lista sem adicionar animal
        finish()
    }
}
