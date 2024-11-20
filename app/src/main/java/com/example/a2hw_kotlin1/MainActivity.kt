package com.example.a2hw_kotlin1

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

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
    // State for holding the list of images and loading state
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // New state for error message
    val scope = rememberCoroutineScope()

    // Function to fetch a random GIF
    fun fetchRandomGif() {
        scope.launch {
            isLoading = true
            errorMessage = null // Reset error message
            try {
                val gifUrl = fetchGifFromApi() // Fetch URL of the GIF
                images = images + gifUrl // Add the new URL to the list of images
            } catch (e: Exception) {
                errorMessage = "Failed to load GIF"
            } finally {
                isLoading = false
            }
        }
    }

    // Layout for displaying the button, loading indicator, and images
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Button to fetch a random GIF
        Button(
            onClick = { fetchRandomGif() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Get Random Gif")
        }

        // Show loading indicator while fetching
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Show error message if the fetch fails
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }


            // Show all fetched GIFs
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(images) { gifUrl ->
                    val painter = rememberAsyncImagePainter(gifUrl)
                    Image(
                        painter = painter,
                        contentDescription = "Random GIF",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)  // Можно настроить высоту, чтобы гифки не были слишком большими
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
