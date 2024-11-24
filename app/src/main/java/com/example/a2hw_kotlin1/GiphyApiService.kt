package com.example.a2hw_kotlin1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

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

object GiphyApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.giphy.com/v1/gifs/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GiphyApi = retrofit.create(GiphyApi::class.java)

    suspend fun fetchGifFromApi(): String {
        try {
            val response = api.getRandomGif()
            return response.data.images.original.url
        } catch (e: Exception) {
            throw e
        }
    }
}
