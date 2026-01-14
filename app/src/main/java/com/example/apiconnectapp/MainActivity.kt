package com.example.apiconnectapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.apiconnectapp.util.NetworkUtils
import com.example.apiconnectapp.weather.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCity: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvDesc: TextView
    private lateinit var weatherCard: View
    private lateinit var emptyStateLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etCity = findViewById(R.id.etCity)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)
        tvCity = findViewById(R.id.tvCity)
        tvTemp = findViewById(R.id.tvTemp)
        tvDesc = findViewById(R.id.tvDesc)
        weatherCard = findViewById(R.id.weatherCard)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)

        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isEmpty()) {
                Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!NetworkUtils.isOnline(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            fetchWeather(city)
        }
    }

    private fun fetchWeather(city: String) {
        progressBar.visibility = View.VISIBLE
        weatherCard.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE

        val api = RetrofitClient.create()
        val apiKey = getString(R.string.openweather_api_key)

        lifecycleScope.launch {
            try {
                val resp = api.getCurrentWeather(city, apiKey)
                tvCity.text = resp.name ?: city
                tvTemp.text = resp.main?.temp?.let { String.format("%.1f°", it) } ?: "-"
                tvDesc.text = resp.weather?.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "-"

                progressBar.visibility = View.GONE
                weatherCard.visibility = View.VISIBLE
                emptyStateLayout.visibility = View.GONE
            } catch (e: IOException) {
                progressBar.visibility = View.GONE
                weatherCard.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: HttpException) {
                progressBar.visibility = View.GONE
                weatherCard.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
                if (e.code() == 404) {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Server error: ${e.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                weatherCard.visibility = View.GONE
                emptyStateLayout.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}