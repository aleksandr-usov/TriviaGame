package com.example.triviagame.remotedata

import com.example.triviagame.api.TriviaApiEntity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaService {

    @GET("/api.php")
    fun getQuestions(
        @Query("amount") amountOfQuestions: Int,
        @Query("category") categoryOfQuestions: String,
        @Query("difficulty") difficultyOfQuestions: String,
        @Query("type") typeOfQuestions: String
    ): Call<TriviaApiEntity>
}