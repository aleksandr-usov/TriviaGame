package com.example.triviagame.remotedata

import com.example.triviagame.data.remote.CategoryApiEntity
import com.example.triviagame.data.remote.TriviaApiEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaService {

    companion object {
        const val BASE_URL = "https://opentdb.com"
    }

    @GET("/api_category.php")
    fun getAllCategories(): Single<CategoryApiEntity>


    @GET("/api.php")
    fun getQuestions(
        @Query("amount") amountOfQuestions: Int,
        @Query("category") categoryOfQuestions: String,
        @Query("difficulty") difficultyOfQuestions: String,
        @Query("type") typeOfQuestions: String
    ): Single<TriviaApiEntity>
}