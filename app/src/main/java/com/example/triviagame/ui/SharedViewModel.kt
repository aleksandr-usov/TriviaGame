package com.example.triviagame.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.triviagame.api.CategoryApiEntity
import com.example.triviagame.api.ResultApiEntity
import com.example.triviagame.api.TriviaApiEntity
import com.example.triviagame.api.TriviaCategoryApiEntity
import com.example.triviagame.remotedata.CategoryService
import com.example.triviagame.remotedata.TriviaService
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SharedViewModel : ViewModel() {

    companion object {
        private const val FIVE_SECONDS = 5L
    }

    val categoriesLiveData = MutableLiveData<List<TriviaCategoryApiEntity>>()
    val questionSetupLiveData = MutableLiveData<QuestionSetup>()
    val secondsRemaining = MutableLiveData<Long>()

    private val questions = mutableListOf<ResultApiEntity>()
    private var currentQuestionIndex = 0

    private var timerDisposable = Disposables.disposed()

    fun onPlayClicked(amount: Int, category: String, difficulty: String, type: String) {
        val client = OkHttpClient.Builder()
        client.addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })

        val retrofitTrivia = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .baseUrl(CategoryService.BASE_URL)
            .build()

        val apiTrivia = retrofitTrivia.create(TriviaService::class.java)
        val categories = categoriesLiveData.value ?: return
        val categoryId = categories.firstOrNull { it.name == category }?.id ?: ""

        apiTrivia
            .getQuestions(amount, categoryId.toString(), difficulty, type)
            .enqueue(object : Callback<TriviaApiEntity> {
                override fun onFailure(call: Call<TriviaApiEntity>, t: Throwable) {
                    Log.d("TAG", "Failed")
                }

                override fun onResponse(
                    call: Call<TriviaApiEntity>,
                    response: Response<TriviaApiEntity>
                ) {
                    questions.clear()
                    questions.addAll(response.body()?.results?.toList() ?: listOf())
                    currentQuestionIndex = 0
                    nextQuestion()
                }
            })
    }

    fun nextQuestion() {
        questions
            .getOrNull(currentQuestionIndex)
            ?.let {
                resetTimer()
                val setup = QuestionSetup(it, ++currentQuestionIndex, questions.size)
                questionSetupLiveData.value = setup
            }
            ?: finishGame()
    }

    private fun finishGame() {

    }

    fun fetchAllCategories() {
        val client = OkHttpClient.Builder()
        client.addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })

        val retrofitCategory = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .baseUrl(CategoryService.BASE_URL)
            .build()

        val apiCategory = retrofitCategory.create(CategoryService::class.java)

        apiCategory.getAllCategories().enqueue(object : Callback<CategoryApiEntity> {
            override fun onFailure(call: Call<CategoryApiEntity>, t: Throwable) {
                Log.d("TAG", "Failed")
            }

            override fun onResponse(
                call: Call<CategoryApiEntity>,
                response: Response<CategoryApiEntity>
            ) {
                val body = response.body() ?: return

                categoriesLiveData.value = body.triviaCategories
                Log.d("TAG", "$body")
            }
        })
    }

    private fun resetTimer() {
        timerDisposable.dispose()

        timerDisposable = Flowable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val remaining = FIVE_SECONDS - it
                    if (remaining > -1) {
                        secondsRemaining.value = remaining
                    } else {
                        nextQuestion()
                    }
                },
                { it.printStackTrace() }
            )
    }

    fun stopTimer() {
        timerDisposable.dispose()
    }

    data class QuestionSetup(
        val question: ResultApiEntity,
        val index: Int,
        val questionsAmount: Int
    )
}