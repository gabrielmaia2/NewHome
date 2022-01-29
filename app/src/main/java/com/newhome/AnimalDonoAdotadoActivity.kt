package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.newhome.dto.Animal

class AnimalDonoAdotadoActivity : AppCompatActivity() {
    private lateinit var animal: Animal

    private lateinit var animalAdotadoDonoImage: ImageView

    private lateinit var nomeAnimalAdotadoDonoText: TextView
    private lateinit var descricaoAnimalAdotadoDonoText: TextView

    private lateinit var perfilAdotadorAdotadoDonoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal_dono_adotado)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalAdotadoDonoImage = findViewById(R.id.animalAdotadoDonoImage)

        nomeAnimalAdotadoDonoText = findViewById(R.id.nomeAnimalAdotadoDonoText)
        descricaoAnimalAdotadoDonoText = findViewById(R.id.descricaoAnimalAdotadoDonoText)

        perfilAdotadorAdotadoDonoButton = findViewById(R.id.perfilAdotadorAdotadoDonoButton)

        carregarDados()
        perfilAdotadorAdotadoDonoButton.setOnClickListener { verAdotador() }
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

        nomeAnimalAdotadoDonoText.text = animal.nome
        descricaoAnimalAdotadoDonoText.text = animal.detalhes
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            animalAdotadoDonoImage.setImageDrawable(drawable)
        }
    }

    private fun verAdotador() {
        // TODO pegar id do adotador do database
        val adotadorId = "id1"

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", adotadorId)
        startActivity(intent)
    }
}