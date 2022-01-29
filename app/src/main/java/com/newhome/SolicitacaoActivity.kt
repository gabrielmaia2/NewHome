package com.newhome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.newhome.dto.Animal
import com.newhome.dto.SolicitacaoID
import com.newhome.dto.Usuario

class SolicitacaoActivity : AppCompatActivity() {
    private lateinit var id: SolicitacaoID
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

        animalDetalhesSolicitacaoFragment = supportFragmentManager.findFragmentById(R.id.animalDetalhesSolicitacaoFragment) as AnimalPreviewFragment

        aceitarSolicitacaoButton = findViewById(R.id.aceitarSolicitacaoButton)
        rejeitarSolicitacaoButton = findViewById(R.id.rejeitarSolicitacaoButton)
        voltarButton = findViewById(R.id.voltarSolicitacaoButton)
        animalBuscadoSolicitacaoButton = findViewById(R.id.animalBuscadoSolicitacaoButton)
        cancelarAdocaoSolicitacaoButton = findViewById(R.id.cancelarAdocaoSolicitacaoButton)

        carregarDados()
        verificarAceita()
        setAdicionarDetalhesLauncher()
        solicitacaoPerfilImage.setOnClickListener { onVerSolicitador() }
        animalDetalhesSolicitacaoFragment.requireView().setOnClickListener { onVerAnimal() }
        aceitarSolicitacaoButton.setOnClickListener { onAceitar() }
        rejeitarSolicitacaoButton.setOnClickListener { onRejeitar() }
        voltarButton.setOnClickListener { onVoltar() }
        animalBuscadoSolicitacaoButton.setOnClickListener { onAnimalBuscado() }
        cancelarAdocaoSolicitacaoButton.setOnClickListener { onCancelar() }
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
        id = intent.getSerializableExtra("id") as SolicitacaoID

        // TODO carregar dados do database

        animal = Animal()
        animal.nome = "Cachorrinho"
        animal.detalhes = "Ele é muito fofinho e gosta de mijar a casa toda."
        animal.imagemURL = ""
        animal.id = id.animalId
        solicitador = Usuario()
        solicitador.nome = "Marcos"
        solicitador.detalhes = "Sou uma pessoa muito legal"
        solicitador.imagemURL = ""
        solicitador.id = id.adotadorId

        Util.tryLoadDrawableAsync(applicationContext, solicitador.imagemURL) { drawable ->
            solicitacaoPerfilImage.setImageDrawable(drawable)
        }
        solicitacaoNomeText.text = solicitador.nome
        solicitacaoPerfilDescricaoText.text = solicitador.detalhes

        // TODO criar animal fragment
        val view = animalDetalhesSolicitacaoFragment.requireView()
        val nomeAnimalPreviewText: TextView = view.findViewById(R.id.nomeAnimalPreviewText)
        val detalhesAnimalPreviewText: TextView = view.findViewById(R.id.detalhesAnimalPreviewText)
        val imageView: ImageView = view.findViewById(R.id.animalImagem)

        nomeAnimalPreviewText.text = animal.nome
        detalhesAnimalPreviewText.text = animal.detalhes
        Util.tryLoadDrawableAsync(applicationContext, animal.imagemURL) { drawable ->
            imageView.setImageDrawable(drawable)
        }
    }

    private fun verificarAceita() {
        // TODO buscar se foi aceita do database
        val aceita = true

        if (aceita) {
            aceitarSolicitacaoButton.visibility = View.GONE
            rejeitarSolicitacaoButton.visibility = View.GONE
            voltarButton.visibility = View.GONE
        } else {
            animalBuscadoSolicitacaoButton.visibility = View.GONE
            cancelarAdocaoSolicitacaoButton.visibility = View.GONE
        }
    }

    private fun setAdicionarDetalhesLauncher() {
        adicionarDetalhesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@registerForActivityResult

                val detalhes = result.data!!.getStringExtra("detalhes")!!
                // TODO enviar detalhes pro database

                val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
                setResult(RESULT_OK, intent)
                intent.putExtra("id", id)
                intent.putExtra("mensagem", "animalBuscado")
                finish()
            }
    }

    private fun onVerSolicitador() {
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

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("mensagem", "aceita")
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onRejeitar() {
        // rejeita solicitacao

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("mensagem", "rejeitada")
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onVoltar() {
        // volta sem fazer nada

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun onAnimalBuscado() {
        // vai pra tela de detalhes do animal

        val intent = Intent(applicationContext, SolicitacaoAdicionarDetalhesActivity::class.java)
        adicionarDetalhesLauncher.launch(intent)
    }

    private fun onCancelar() {
        // cancela adocao

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("mensagem", "cancelado")
        setResult(RESULT_OK, intent)
        finish()
    }
}