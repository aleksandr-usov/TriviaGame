package com.example.triviagame.data.repository

import com.example.triviagame.data.remote.CategoryApiEntity
import com.example.triviagame.data.remote.TriviaApiEntity
import com.example.triviagame.remotedata.TriviaService
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object TriviaRepository {

    private val service: TriviaService by lazy {
        val client = OkHttpClient.Builder()
        client.addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })

        val retrofitCategory = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client.build())
            .baseUrl(TriviaService.BASE_URL)
            .build()

        retrofitCategory.create(TriviaService::class.java)
    }

    fun getCategories(): Single<CategoryApiEntity> =
        service.getAllCategories()

    fun getQuestions(
        amountOfQuestions: Int,
        categoryOfQuestions: String,
        difficultyOfQuestions: String,
        typeOfQuestions: String
    ): Single<TriviaApiEntity> =
        service.getQuestions(
            amountOfQuestions,
            categoryOfQuestions,
            difficultyOfQuestions,
            typeOfQuestions
        )
}