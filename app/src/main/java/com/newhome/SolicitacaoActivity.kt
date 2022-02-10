package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Animal
import com.newhome.dto.Solicitacao
import com.newhome.dto.SolicitacaoID
import com.newhome.dto.Usuario

class SolicitacaoActivity : AppCompatActivity() {
    private lateinit var solicitacao: Solicitacao
    private lateinit var solicitador: Usuario
    private lateinit var animal: Animal

    private lateinit var solicitacaoPerfilImage: ImageView

    private lateinit var solicitacaoNomeText: TextView
    private lateinit var solicitacaoPerfilDescricaoText: TextView

    private lateinit var animalDetalhesSolicitacaoFragment: AnimalPreviewFragment

    private lateinit var aceitarSolicitacaoButton: Button
    private lateinit var rejeitarSolicitacaoButton: Button
    private lateinit var voltarButton: Button
    private lateinit var animalBuscadoSolicitacaoButton: Button
    private lateinit var cancelarAdocaoSolicitacaoButton: Button

    private lateinit var adicionarDetalhesLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitacao)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Solicitação de adoção"

        solicitacaoPerfilImage = findViewById(R.id.solicitacaoPerfilImage)

        solicitacaoNomeText = findViewById(R.id.solicitacaoNomeText)
        solicitacaoPerfilDescricaoText = findViewById(R.id.solicitacaoPerfilDescricaoText)

        animalDetalhesSolicitacaoFragment =
            supportFragmentManager.findFragmentById(R.id.animalDetalhesSolicitacaoFragment) as AnimalPreviewFragment

        aceitarSolicitacaoButton = findViewById(R.id.aceitarSolicitacaoButton)
        rejeitarSolicitacaoButton = findViewById(R.id.rejeitarSolicitacaoButton)
        voltarButton = findViewById(R.id.voltarSolicitacaoButton)
        animalBuscadoSolicitacaoButton = findViewById(R.id.animalBuscadoSolicitacaoButton)
        cancelarAdocaoSolicitacaoButton = findViewById(R.id.cancelarAdocaoSolicitacaoButton)

        carregarDados()
        setAdicionarDetalhesLauncher()
        solicitacaoPerfilImage.setOnClickListener { onVerSolicitador() }
        animalDetalhesSolicitacaoFragment.requireView().setOnClickListener { onVerAnimal() }
        aceitarSolicitacaoButton.setOnClickListener { onAceitar() }
        rejeitarSolicitacaoButton.setOnClickListener { onRejeitar() }
        voltarButton.setOnClickListener { onVoltar() }
        animalBuscadoSolicitacaoButton.setOnClickListener { onAnimalBuscado() }
        cancelarAdocaoSolicitacaoButton.setOnClickListener { onCancelar() }

        animalBuscadoSolicitacaoButton.visibility = View.GONE
        cancelarAdocaoSolicitacaoButton.visibility = View.GONE
        aceitarSolicitacaoButton.visibility = View.GONE
        rejeitarSolicitacaoButton.visibility = View.GONE
        voltarButton.visibility = View.GONE
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
        val id = intent.getSerializableExtra("id") as SolicitacaoID

        NewHomeApplication.solicitacaoProvider.getSolicitacao(id, { solicitacao ->
            this.solicitacao = solicitacao
            animal = solicitacao.animal
            solicitador = solicitacao.solicitador

            solicitacaoNomeText.text = solicitador.nome
            solicitacaoPerfilDescricaoText.text = solicitador.detalhes
            solicitacaoPerfilImage.setImageBitmap(solicitador.imagem)

            // cria animal fragment
            val view = animalDetalhesSolicitacaoFragment.requireView()
            val nomeAnimalPreviewText: TextView = view.findViewById(R.id.nomeAnimalPreviewText)
            val detalhesAnimalPreviewText: TextView =
                view.findViewById(R.id.detalhesAnimalPreviewText)
            val imageView: ImageView = view.findViewById(R.id.animalImagem)

            nomeAnimalPreviewText.text = animal.nome
            detalhesAnimalPreviewText.text = animal.detalhes
            imageView.setImageBitmap(animal.imagem)

            verificarAceita()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun verificarAceita() {
        NewHomeApplication.solicitacaoProvider.getStatusSolicitacao(
            solicitacao.id!!,
            { statusSolicitacao ->
                if (statusSolicitacao.solicitacaoAceita) {
                    animalBuscadoSolicitacaoButton.visibility = View.VISIBLE
                    cancelarAdocaoSolicitacaoButton.visibility = View.VISIBLE
                } else {
                    aceitarSolicitacaoButton.visibility = View.VISIBLE
                    rejeitarSolicitacaoButton.visibility = View.VISIBLE
                    voltarButton.visibility = View.VISIBLE
                }
            },
            { e ->
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun setAdicionarDetalhesLauncher() {
        adicionarDetalhesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val detalhes = result.data!!.getStringExtra("detalhes")!!

                NewHomeApplication.solicitacaoProvider.aceitarSolicitacao(solicitacao.id!!,
                    detalhes,
                    {
                        Toast.makeText(
                            applicationContext,
                            "Solicitação aceita.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    { e ->
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    })
            }
    }

    private fun onVerSolicitador() {
        // vai pra tela do solicitador

        val intent = Intent(applicationContext, PerfilActivity::class.java)
        intent.putExtra("id", solicitador.id)
        startActivity(intent)
    }

    private fun onVerAnimal() {
        // vai pra tela do animal dono

        val intent = Intent(applicationContext, AnimalDonoActivity::class.java)
        intent.putExtra("id", animal.id)
        startActivity(intent)
    }

    private fun onAceitar() {
        // aceita solicitacao

        val intent = Intent(applicationContext, SolicitacaoAdicionarDetalhesActivity::class.java)
        adicionarDetalhesLauncher.launch(intent)
    }

    private fun onRejeitar() {
        // rejeita solicitacao

        NewHomeApplication.solicitacaoProvider.rejeitarSolicitacao(solicitacao.id!!, {
            Toast.makeText(applicationContext, "Solicitação rejeitada.", Toast.LENGTH_SHORT).show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onVoltar() {
        // volta sem fazer nada

        finish()
    }

    private fun onAnimalBuscado() {
        // vai pra tela de detalhes do animal

        NewHomeApplication.animalProvider.animalEnviado(animal.id, {
            Toast.makeText(applicationContext, "Animal buscado.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onCancelar() {
        // cancela adocao

        NewHomeApplication.solicitacaoProvider.cancelarSolicitacaoAceita(animal.id, {
            Toast.makeText(applicationContext, "Cancelado.", Toast.LENGTH_SHORT).show()
            finish()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }
}
