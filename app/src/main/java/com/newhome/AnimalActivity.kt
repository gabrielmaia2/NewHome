package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.newhome.dto.Animal
import com.newhome.dto.Usuario

class AnimalActivity : AppCompatActivity() {
    private lateinit var id: String
    private lateinit var dono: Usuario
    private lateinit var animal: Animal

    private lateinit var animalImage: ImageView

    private lateinit var nomeAnimalText: TextView
    private lateinit var descricaoAnimalText: TextView

    private lateinit var imagemDono: ImageView

    private lateinit var verMapaAnimalButton: Button
    private lateinit var verDetalhesBuscaButton: Button

    private lateinit var solicitacarAdocaoButton: Button
    private lateinit var animalBuscadoButton: Button
    private lateinit var cancelarAdocaoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Animal"

        animalImage = findViewById(R.id.animalImage)

        nomeAnimalText = findViewById(R.id.nomeAnimalText)
        descricaoAnimalText = findViewById(R.id.descricaoAnimalText)

        imagemDono = findViewById(R.id.imagemDono)

        verMapaAnimalButton = findViewById(R.id.verMapaAnimalButton)
        verDetalhesBuscaButton = findViewById(R.id.verDetalhesBuscaButton)

        animalBuscadoButton = findViewById(R.id.animalBuscadoButton)
        cancelarAdocaoButton = findViewById(R.id.cancelarAdocaoButton)
        solicitacarAdocaoButton = findViewById(R.id.solicitacarAdocaoButton)

        carregarAnimal()
        verificarSolicitado()
        imagemDono.setOnClickListener { onVerDono() }
        verMapaAnimalButton.setOnClickListener { onVerMapa() }
        verDetalhesBuscaButton.setOnClickListener { onVerDetalhes() }
        animalBuscadoButton.setOnClickListener { onAnimalBuscado() }
        cancelarAdocaoButton.setOnClickListener { onCancelarAdocao() }
        solicitacarAdocaoButton.setOnClickListener { onSolicitarAdocao() }
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

    private fun carregarAnimal() {
        // TODO carregar do database

        animal = Animal()
        animal.id = intent.getStringExtra("id")!!
        animal.nome = "Cachorrinho"
        animal.detalhes = "Ele é muito fofinho e gosta de mijar a casa toda."
        animal.imagemURL = ""

        nomeAnimalText.text = animal.nome
        descricaoAnimalText.text = animal.detalhes
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            animalImage.setImageDrawable(drawable)
        }
    }

    private fun verificarSolicitado() {
        // TODO verificar se o animal foi solicitado e se a solicitacao foi aceita

        val animalSolicitado = false
        val solicitacaoAceita = false

        if (!solicitacaoAceita) {
            verDetalhesBuscaButton.visibility = View.GONE
            verMapaAnimalButton.visibility = View.GONE
        }

        if (animalSolicitado) {
            solicitacarAdocaoButton.visibility = View.GONE
        } else {
            animalBuscadoButton.visibility = View.GONE
            cancelarAdocaoButton.visibility = View.GONE
        }
    }

    private fun onVerDono() {
        // vai pra tela de ver dono

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", dono.id)
        startActivity(intent)
    }

    private fun onVerMapa() {
        // vai pra tela do mapa

        // TODO mostrar mapa
    }

    private fun onVerDetalhes() {
        // vai pra tela de detalhes de detalhes


    }

    private fun onAnimalBuscado() {
        // busca animal e vai pra tela de animal adotado

        // TODO enviar pro database que o animal foi buscado

        val intent = Intent(applicationContext, AnimalAdotadoActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onCancelarAdocao() {
        // cancela adocao

        // TODO enviar cancelamento pro database

        verificarSolicitado()
    }

    private fun onSolicitarAdocao() {
        // solicita adocao

        // TODO enviar solicitacao pro database

        verificarSolicitado()
    }
}