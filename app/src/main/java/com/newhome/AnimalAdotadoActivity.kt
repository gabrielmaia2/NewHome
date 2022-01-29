package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.newhome.dto.Animal

class AnimalAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalAdotadoImage: ImageView

    private lateinit var nomeAnimalAdotadoText: TextView
    private lateinit var descricaoAnimalAdotadoText: TextView

    private lateinit var perfilDonoAntigoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalAdotadoImage = findViewById(R.id.animalAdotadoImage)

        nomeAnimalAdotadoText = findViewById(R.id.nomeAnimalAdotadoText)
        descricaoAnimalAdotadoText = findViewById(R.id.descricaoAnimalAdotadoText)

        perfilDonoAntigoButton = findViewById(R.id.perfilDonoAntigoButton)

        carregarDados()
        perfilDonoAntigoButton.setOnClickListener { onVerPerfilDonoAntigo() }
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

    private fun carregarDados() {
        // TODO carregar animal do database

        animal = Animal()
        animal.id = intent.getStringExtra("id")!!
        animal.imagemURL = ""
        animal.nome = "Cachorrinho"
        animal.detalhes = "Ele é muito fofinho e gosta de mijar a casa toda."

        nomeAnimalAdotadoText.text = animal.nome
        descricaoAnimalAdotadoText.text = animal.detalhes
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            animalAdotadoImage.setImageDrawable(drawable)
        }
    }

    private fun onVerPerfilDonoAntigo() {
        // TODO pegar id do dono antigo do database
        val donoAntigoId = "id1"

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", donoAntigoId)
        startActivity(intent)
    }
}