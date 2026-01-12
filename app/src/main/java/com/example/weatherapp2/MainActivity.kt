package com.example.weatherapp2
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp2.ui.theme.WEATHERAPP2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val weatherService = WeatherService.create()
    private val apiKey = "f2c686d1d7ca49b96fb848e288a14cad"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WEATHERAPP2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(
                        modifier = Modifier.padding(innerPadding),
                        weatherService = weatherService,
                        apiKey = apiKey
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, weatherService: WeatherService, apiKey: String) {
    var city by remember { mutableStateOf("") }
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter City") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (city.isNotBlank()) {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            weatherResponse = weatherService.getWeather(city, apiKey)
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage ?: "Unknown error"}"
                            weatherResponse = null
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Weather")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else {
            weatherResponse?.let {
                Text(text = it.cityName, fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
                Text(text = "${it.main.temp}°C", fontSize = 48.sp, style = MaterialTheme.typography.displayLarge)
                Text(text = it.weather.firstOrNull()?.description?.replaceFirstChar { char -> char.uppercase() } ?: "", fontSize = 20.sp)
            }
        }
    }
}
