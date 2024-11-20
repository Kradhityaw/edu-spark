package com.example.eduspark

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eduspark.databinding.ActivityGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    private lateinit var bind: ActivityGameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityGameBinding.inflate(layoutInflater)
        setContentView(bind.root)

        GlobalScope.launch(Dispatchers.IO) {
            val conn = URL("${Url.API_URL}/words/${intent.getStringExtra("gameId")}").openStream().bufferedReader().readText()
            val arrayData = JSONArray(conn)
            var index = 0
            val image = BitmapFactory.decodeStream(URL("${Url.IMAGE_URL}/images/${arrayData.getJSONObject(index).getString("image")}").openStream())

            runOnUiThread {
                var gameProgress = JSONArray()

                // Default game
                bind.quizImage.setImageBitmap(image)
                val questionFirst = arrayData.getJSONObject(index).getString("word")
                bind.quizQuestion.text = shuffleString(questionFirst)

                for (e in 0 until arrayData.length()) {
                    // Add sample data to JSON ARRAy
                    gameProgress.apply {
                        put(JSONObject().apply {
                            put("id", arrayData.getJSONObject(e).getInt("id"))
                            put("answer", "")
                            put("nilai", 0)
                        })
                    }
                }

                // Next game
                bind.nextBtn.setOnClickListener {
                    var hasil = bind.quizAnswer.text.toString().uppercase()
                    var nilai = if (arrayData.getJSONObject(index).getString("word") == hasil) 10 else 0

//                    if (arrayData.getJSONObject(index).getInt("id") == gameProgress.getJSONObject(index).getInt("id")) {
                        gameProgress.getJSONObject(index).apply {
                            put("id", arrayData.getJSONObject(index).getInt("id"))
                            put("answer", bind.quizAnswer.text.toString())
                            put("nilai", nilai)
                        }
//                    } else {
//                        gameProgress.apply {
//                            put(JSONObject().apply {
//                                put("id", arrayData.getJSONObject(index).getInt("id"))
//                                put("answer", bind.quizAnswer.text.toString())
//                                put("nilai", nilai)
//                            })
//                        }
//                    }

                    Log.d("bejirlah", gameProgress.toString())

                    index += 1
                    if (index == arrayData.length() - 1) bind.nextBtn.text = "FINISH"
                    bind.prevBtn.isEnabled = true
                    if (index == arrayData.length()) {
                        var totalNilai = 0
                        for (e in 0 until gameProgress.length()) {
                            totalNilai += gameProgress.getJSONObject(e).getInt("nilai")
                        }
                        startActivity(Intent(this@GameActivity, ScoreActivity::class.java).apply {
                            putExtra("gameId", intent.getStringExtra("gameId")?.toInt())
                            putExtra("totalPoint", totalNilai)
                        })
                        gameProgress = JSONArray("[]")
                        finish()
                        return@setOnClickListener
                    }

                    // Get input back
                    if (gameProgress.getJSONObject(index).getString("answer") == "") {
                        bind.quizAnswer.text.clear()
                    } else {
                        bind.quizAnswer.setText(gameProgress.getJSONObject(index).getString("answer"))
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        val image = BitmapFactory.decodeStream(URL("${Url.IMAGE_URL}/images/${arrayData.getJSONObject(index).getString("image")}").openStream())

                        runOnUiThread {
                            bind.quizImage.setImageBitmap(image)
                            val questionFirst = arrayData.getJSONObject(index).getString("word")
                            bind.quizQuestion.text = shuffleString(questionFirst)
                        }
                    }
                }

                // Prev game
                bind.prevBtn.setOnClickListener {
                    var hasil = bind.quizAnswer.text.toString().uppercase()
                    var nilai = if (arrayData.getJSONObject(index).getString("word") == hasil) 10 else 0

//                    if (arrayData.getJSONObject(index).getInt("id") == gameProgress.getJSONObject(index).getInt("id")) {
                    gameProgress.getJSONObject(index).apply {
                        put("id", arrayData.getJSONObject(index).getInt("id"))
                        put("answer", bind.quizAnswer.text.toString())
                        put("nilai", nilai)
                    }

                    index -= 1
                    if (index == 0) bind.prevBtn.isEnabled = false

                    bind.nextBtn.text = "NEXT"

                    // Get input back
                    if (gameProgress.getJSONObject(index).getString("answer") == "") {
                        bind.quizAnswer.text.clear()
                    } else {
                        bind.quizAnswer.setText(gameProgress.getJSONObject(index).getString("answer"))
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        val image = BitmapFactory.decodeStream(URL("${Url.IMAGE_URL}/images/${arrayData.getJSONObject(index).getString("image")}").openStream())

                        runOnUiThread {
                            bind.quizImage.setImageBitmap(image)
                            val questionFirst = arrayData.getJSONObject(index).getString("word")
                            bind.quizQuestion.text = shuffleString(questionFirst)
                        }
                    }
                }
            }
        }
    }

    private fun shuffleString(question: String) : String {
        val chars = question.toList().toMutableList()
        chars.shuffle(Random)
        return chars.joinToString("")
    }
}