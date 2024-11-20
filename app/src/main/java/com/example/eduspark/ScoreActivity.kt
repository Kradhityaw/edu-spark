package com.example.eduspark

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eduspark.databinding.ActivityScoreBinding
import com.example.eduspark.databinding.LeaderboardLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ScoreActivity : AppCompatActivity() {
    lateinit var bind: ActivityScoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val gameId = intent.getIntExtra("gameId", 0)
        val totalPoint = intent.getIntExtra("totalPoint", 0)

        bind.quizScore.text = totalPoint.toString()

        loadLeaderboard()

        bind.submitBtn.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val conn = URL("${Url.API_URL}/leaderboards").openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")

                val data = JSONObject().apply {
                    put("gameID", gameId)
                    put("nickname", bind.quizNickname.text.toString())
                    put("totalPoint", totalPoint)
                }

                conn.outputStream.write(data.toString().toByteArray())
                conn.outputStream.close()
                conn.outputStream.flush()

                val responseCode = conn.responseCode

                if (responseCode in 200..299) {
                    loadLeaderboard()
                    runOnUiThread {
                        bind.submitBtn.isEnabled = false
                    }
                }
            }
        }

        bind.leadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    fun loadLeaderboard() {
        val gameId = intent.getIntExtra("gameId", 0)

        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Url.API_URL}/leaderboards/${gameId}").openStream().bufferedReader().readText()
            val jsonData = JSONArray(conn)

            runOnUiThread {
                bind.rvScore.adapter = object : RecyclerView.Adapter<LeaderboardAdapter>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardAdapter {
                        val inflate = LeaderboardLayoutBinding.inflate(layoutInflater, parent, false)
                        return LeaderboardAdapter(inflate)
                    }

                    override fun getItemCount(): Int = jsonData.length()

                    override fun onBindViewHolder(holder: LeaderboardAdapter, position: Int) {
                        val getData = jsonData.getJSONObject(position)
                        holder.binding.quizLeaderboardName.text = "${position + 1}. ${getData.getString("nickname")}"
                        holder.binding.quizLeaderboardScore.text = getData.getString("totalPoint")
                    }
                }
                bind.rvScore.layoutManager = LinearLayoutManager(this@ScoreActivity)
            }
        }
    }

    class LeaderboardAdapter(val binding: LeaderboardLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}