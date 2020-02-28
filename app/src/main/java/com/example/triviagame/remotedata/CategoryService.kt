package com.example.triviagame.remotedata

import com.example.triviagame.api.CategoryApiEntity
import retrofit2.Call
import retrofit2.http.GET

interface CategoryService {

    @GET("/api_category.php")
    fun getAllCategories(): Call<CategoryApiEntity>

    companion object {
        const val BASE_URL = "https://opentdb.com"
    }
}