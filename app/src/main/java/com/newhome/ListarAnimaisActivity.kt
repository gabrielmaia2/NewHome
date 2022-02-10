package com.newhome

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.newhome.dto.Animal
import com.newhome.dto.Usuario

class ListarAnimaisActivity : AppCompatActivity() {
    private lateinit var semAnimaisText: TextView

    private lateinit var porAdocaoButton: Button
    private lateinit var solicitacoesFeitasListarAnimaisButton: Button
    private lateinit var solicitacoesRecebidasListarAnimaisButton: Button

    private lateinit var listView: ListView
    private lateinit var listViewAdapter: AnimalAdapter

    // todosAnimais, postosAdocao, adotados, solicitados
    private var tipo: String = ""

    // quando o tipo e postosAdocao, se esse id nao for vazio, vai mostrar os animais
    // postos em adocao pelo usuario com esse id
    private var usuarioId: String = ""
    private var eProprioPerfil = false
    private lateinit var usuarioAtual: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_animais)

        semAnimaisText = findViewById(R.id.semAnimaisText)
        semAnimaisText.visibility = View.GONE

        porAdocaoButton = findViewById(R.id.porAdocaoListarAnimaisButton)
        solicitacoesFeitasListarAnimaisButton =
            findViewById(R.id.solicitacoesFeitasListarAnimaisButton)
        solicitacoesRecebidasListarAnimaisButton =
            findViewById(R.id.solicitacoesRecebidasListarAnimaisButton)

        listView = findViewById(R.id.listarAnimaisListView)

        listView.setOnItemClickListener { _, _, position, _ -> onVerAnimal(position) }
        porAdocaoButton.setOnClickListener { onAddAnimal() }
        solicitacoesFeitasListarAnimaisButton.setOnClickListener { onVerSolicitacoesFeitas() }
        solicitacoesRecebidasListarAnimaisButton.setOnClickListener { onVerSolicitacoesRecebidas() }

        porAdocaoButton.visibility = View.GONE
        solicitacoesFeitasListarAnimaisButton.visibility = View.GONE
        solicitacoesRecebidasListarAnimaisButton.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.pesquisaMenuItem)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Pesquise por um animal"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarAnimais(newText)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.verPerfilMenuItem -> {
                val intent = Intent(applicationContext, PerfilActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun carregarDados() {
        // pega qual o tipo da lista (todosAnimais por padrao)
        tipo = intent.getStringExtra("tipo") ?: "todosAnimais"

        // pega o id do usuario (se for postosAdocao)
        usuarioId = intent.getStringExtra("usuarioId") ?: ""

        if (usuarioId == "") usuarioId = NewHomeApplication.idUsuarioAtual
        eProprioPerfil = usuarioId == NewHomeApplication.idUsuarioAtual

        if (!eProprioPerfil) {
            tipo = "postosAdocao"
        }

        NewHomeApplication.usuarioProvider.getUsuarioAtual({ usuario ->
            usuarioAtual = usuario

            definirVisibilidadeBotoes()
            carregarAnimais()
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun definirVisibilidadeBotoes() {
        // muda quais botoes estao visiveis dependendo do tipo da tela
        // se o tipo for todosAnimals ou postosAdocao, o botao de por animal em adocao fica visivel
        // se o tipo for adotados ou postosAdocao, o botao de solicitacoes feitas fica visivel

        // verifica se e a lista de todos os animais ou
        // a de animais postos em adocao do proprio perfil

        if (tipo == "todosAnimais" || (tipo == "postosAdocao" && eProprioPerfil)) {
            // permite adicionar animal pra adocao
            porAdocaoButton.visibility = View.VISIBLE
            solicitacoesRecebidasListarAnimaisButton.visibility = View.VISIBLE
        }

        // verifica se e a lista de animais adotados ou solicitados
        if (tipo == "adotados" || (tipo == "postosAdocao" && eProprioPerfil)) {
            // permite ver solicitacoes feitas por este no adotados ou por outros no postosAdocao
            solicitacoesFeitasListarAnimaisButton.visibility = View.VISIBLE
        }
    }

    private fun onVerAnimal(position: Int) {
        // vai pra tela de ver animal

        val animal = listViewAdapter.getItem(position)!!

        NewHomeApplication.animalProvider.getDonoInicial(animal.id, { dono ->
            // verifica se e dono
            val eDono = usuarioAtual.id == dono.id

            NewHomeApplication.animalProvider.getAdotador(animal.id, { adotador ->
                    // verifica se animal foi adotado
                    val adotado = adotador != null

                    val intent = if (eDono) {
                        if (adotado) {
                            Intent(applicationContext, AnimalDonoAdotadoActivity::class.java)
                        } else {
                            Intent(applicationContext, AnimalDonoActivity::class.java)
                        }
                    } else {
                        if (adotado) {
                            Intent(applicationContext, AnimalAdotadoActivity::class.java)
                        } else {
                            Intent(applicationContext, AnimalActivity::class.java)
                        }
                    }

                    intent.putExtra("id", animal.id)
                    startActivity(intent)
                }, { e ->
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                })
        }, { e ->
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onAddAnimal() {
        // vai para a tela de adicionar um novo animal

        val intent = Intent(applicationContext, NovoAnimalActivity::class.java)
        startActivity(intent)
    }

    private fun onVerSolicitacoesFeitas() {
        // vai para tela de solicitacoes feitas pela pessoa

        val intent = Intent(applicationContext, ListarAnimaisActivity::class.java)
        intent.putExtra("tipo", "solicitados")
        startActivity(intent)
    }

    private fun onVerSolicitacoesRecebidas() {
        // vai para tela de todas solicitacoes de adocao recebidas

        val intent = Intent(applicationContext, ListaSolicitacaoActivity::class.java)
        startActivity(intent)
    }

    private fun carregarAnimais() {
        // carrega animais do database

        if (tipo == "todosAnimais") {
            NewHomeApplication.animalProvider.getTodosAnimais({ animais ->
                onAnimaisCarregados(animais)
            }, { e ->
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            })
        } else if (tipo == "postosAdocao") {
            NewHomeApplication.animalProvider.getAnimaisPostosAdocao(usuarioId, { animais ->
                onAnimaisCarregados(animais)
            }, { e ->
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            })
        } else if (tipo == "adotados") {
            NewHomeApplication.animalProvider.getAnimaisAdotados(usuarioAtual.id, { animais ->
                    onAnimaisCarregados(animais)
                }, { e ->
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                })
        } else if (tipo == "solicitados") {
            NewHomeApplication.animalProvider.getAnimaisSolicitados(usuarioAtual.id, { animais ->
                    onAnimaisCarregados(animais)
                }, { e ->
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                })
        }
    }

    private fun onAnimaisCarregados(animais: List<Animal>) {
        listViewAdapter = AnimalAdapter(this, animais)
        listView.adapter = listViewAdapter

        if (listViewAdapter.isEmpty) {
            semAnimaisText.visibility = View.VISIBLE
        } else {
            semAnimaisText.visibility = View.GONE
        }
    }

    private fun filtrarAnimais(pesquisa: String?) {
        listViewAdapter.filter.filter(pesquisa?.lowercase()) { count ->
            if (count == 0) {
                semAnimaisText.visibility = View.VISIBLE
            } else {
                semAnimaisText.visibility = View.GONE
            }
        }
    }
}
