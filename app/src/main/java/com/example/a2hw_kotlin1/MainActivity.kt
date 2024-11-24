package com.example.a2hw_kotlin1

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration

// API Interface
interface GiphyApi {
    @GET("random?api_key=lh4RYRGvcL5h2C84BMVJgA2zIZ7cHjpX&tag=&rating=g")
    suspend fun getRandomGif(): GifResponse
}

// Data models
data class GifResponse(val data: GifData)
data class GifData(val images: GifImages)
data class GifImages(val original: GifOriginal)
data class GifOriginal(val url: String)

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GiphyApp() // Calling composable function inside the setContent block
            }
        }
    }
}

// Composable function for the app
@Composable
fun GiphyApp() {
    // Состояние для хранения списка изображений, состояния загрузки и сообщения об ошибке
    var images by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Функция для загрузки случайного GIF
    fun fetchRandomGif() {
        scope.launch {
            isLoading = true
            errorMessage = null // Сбрасываем сообщение об ошибке
            try {
                val gifUrl = fetchGifFromApi() // Загружаем URL GIF
                images = images + gifUrl // Добавляем новый URL в список изображений
            } catch (e: Exception) {
                errorMessage = "Failed to load GIF"
            } finally {
                isLoading = false
            }
        }
    }

    // Получаем информацию о текущей ориентации экрана
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Разметка для отображения кнопки, индикатора загрузки и изображений
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Кнопка для получения случайного GIF
        Button(
            onClick = { fetchRandomGif() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Get Random Gif")
        }

        // Показать индикатор загрузки, если идет загрузка
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Показать сообщение об ошибке, если загрузка не удалась
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            // Отображаем все загруженные GIF в сетке
            LazyVerticalGrid(
                columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(images) { gifUrl ->
                    val painter = rememberAsyncImagePainter(gifUrl)
                    Image(
                        painter = painter,
                        contentDescription = "Random GIF",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)  // Настройте высоту изображений
                            .padding(4.dp)   // Добавляем отступы между картинками
                    )
                }
            }
        }
    }
}

// Function to fetch the GIF URL from the API
suspend fun fetchGifFromApi(): String {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.giphy.com/v1/gifs/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(GiphyApi::class.java)

    try {
        val response = api.getRandomGif()
        return response.data.images.original.url
    } catch (e: Exception) {
        Log.e("GiphyApp", "Error fetching GIF from API", e)
        throw e // Пробрасываем исключение, чтобы оно было обработано в UI
    }
}
