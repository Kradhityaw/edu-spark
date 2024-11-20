package com.example.eduspark

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eduspark.databinding.ActivityMainBinding
import com.example.eduspark.databinding.CardLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        loadGames()
    }

    private fun loadGames() {
        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Url.API_URL}/games").openStream().bufferedReader().readText()
            val dataArray = JSONArray(conn)

            runOnUiThread {
                bind.rvHome.adapter = object : RecyclerView.Adapter<CardHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
                        val inflate = CardLayoutBinding.inflate(layoutInflater, parent, false)
                        return CardHolder(inflate)
                    }

                    override fun getItemCount(): Int = dataArray.length()

                    override fun onBindViewHolder(holder: CardHolder, position: Int) {
                        val getObject = dataArray.getJSONObject(position)
                        holder.binding.quizName.text = getObject.getString("name")
                        holder.binding.quizCategory.text = getObject.getString("category")
                        holder.binding.quizPlayers.text = "${getObject.getString("totalPlayer")} Players"

                        holder.itemView.setOnClickListener {
                            startActivity(Intent(this@MainActivity, GameActivity::class.java).apply {
                                putExtra("gameId", getObject.getString("id"))
                            })
                        }
                    }
                }
                bind.rvHome.layoutManager = LinearLayoutManager(this@MainActivity)
            }
        }
    }

    class CardHolder(val binding:CardLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}