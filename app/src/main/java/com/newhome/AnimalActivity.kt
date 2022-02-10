package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Animal
import com.newhome.dto.SolicitacaoID
import com.newhome.dto.Usuario

class AnimalActivity : AppCompatActivity() {
    private lateinit var animalImage: ImageView

    private lateinit var nomeAnimalText: TextView

    private lateinit var descricaoAnimalText: TextView
    private lateinit var imagemDono: ImageView

    private lateinit var verMapaAnimalButton: Button

    private lateinit var verDetalhesBuscaButton: Button
    private lateinit var solicitacarAdocaoButton: Button

    private lateinit var animalBuscadoButton: Button
    private lateinit var cancelarAdocaoButton: Button

    private lateinit var dono: Usuario
    private lateinit var animal: Animal
    private var detalhesAdocao: String = ""

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

        carregarDados()
        imagemDono.setOnClickListener { onVerDono() }
        verMapaAnimalButton.setOnClickListener { onVerMapa() }
        verDetalhesBuscaButton.setOnClickListener { onVerDetalhes() }
        animalBuscadoButton.setOnClickListener { onAnimalBuscado() }
        cancelarAdocaoButton.setOnClickListener { onCancelarAdocao() }
        solicitacarAdocaoButton.setOnClickListener { onSolicitarAdocao() }

        animalBuscadoButton.visibility = View.GONE
        cancelarAdocaoButton.visibility = View.GONE
        solicitacarAdocaoButton.visibility = View.GONE
        verDetalhesBuscaButton.visibility = View.GONE
        verMapaAnimalButton.visibility = View.GONE
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
        // carrega dados do database

        val id = intent.getStringExtra("id")!!
        NewHomeApplication.animalProvider.getAnimal(id, { animal ->
            this.animal = animal
            nomeAnimalText.text = animal.nome
            descricaoAnimalText.text = animal.detalhes
            animalImage.setImageBitmap(animal.imagem)

            verificarSolicitado()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })

        NewHomeApplication.animalProvider.getDonoInicial(id, { usuario ->
            dono = usuario
            imagemDono.setImageBitmap(dono.imagem)
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun verificarSolicitado() {
        // verifica se o animal foi solicitado e se a solicitacao foi aceita

        NewHomeApplication.usuarioProvider.getUsuarioAtual({ usuario ->
            val solicitacaoId = SolicitacaoID()
            solicitacaoId.adotadorId = usuario.id
            solicitacaoId.animalId = animal.id

            NewHomeApplication.solicitacaoProvider.getStatusSolicitacao(
                solicitacaoId,
                { statusSolicitacao ->
                    detalhesAdocao = statusSolicitacao.detalhesAdocao

                    if (!statusSolicitacao.solicitado) {
                        solicitacarAdocaoButton.visibility = View.VISIBLE
                        return@getStatusSolicitacao
                    }

                    cancelarAdocaoButton.visibility = View.VISIBLE

                    if (statusSolicitacao.solicitacaoAceita) {
                        animalBuscadoButton.visibility = View.VISIBLE
                        verDetalhesBuscaButton.visibility = View.VISIBLE
                        verMapaAnimalButton.visibility = View.VISIBLE
                    }
                },
                { e ->
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                })
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
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
        // vai pra tela de detalhes

        // TODO
        val intent = Intent(applicationContext, SolicitacaoDetalhesActivity::class.java)
        intent.putExtra("detalhes", detalhesAdocao)
        startActivity(intent)
    }

    private fun onAnimalBuscado() {
        // busca animal e vai pra tela de animal adotado

        NewHomeApplication.animalProvider.animalBuscado(animal.id, {
            Toast.makeText(applicationContext, "Animal buscado.", Toast.LENGTH_SHORT).show()

            val intent = Intent(applicationContext, AnimalAdotadoActivity::class.java)
            intent.putExtra("id", animal.id)
            startActivity(intent)
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCancelarAdocao() {
        // cancela adocao

        NewHomeApplication.solicitacaoProvider.cancelarSolicitacao(animal.id, {
            Toast.makeText(applicationContext, "Solicitação cancelada.", Toast.LENGTH_SHORT).show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onSolicitarAdocao() {
        // solicita adocao

        NewHomeApplication.solicitacaoProvider.solicitarAnimal(animal.id, {
            Toast.makeText(applicationContext, "Animal solicitado.", Toast.LENGTH_SHORT).show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }
}
