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
import com.example.triviagame.ui.SharedViewModel.CurrentlySelecting.*
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
        private const val THIRTY_SECONDS = 30L
    }

    val numberOfQuestions = MutableLiveData<Int>()
    private val anyCategory = TriviaCategoryApiEntity(-1, "Any")
    val numberOfCorrectAnswers = MutableLiveData<Int>()
    val screen = MutableLiveData<GameScreen>()
    val categoriesLiveData = MutableLiveData<List<TriviaCategoryApiEntity>>()
    val questionSetupLiveData = MutableLiveData<QuestionSetup>()
    val secondsRemaining = MutableLiveData<Long>()
    val currentCategory = MutableLiveData<TriviaCategoryApiEntity>()
    val currentDifficulty = MutableLiveData<Difficulty>()
    val currentMode = MutableLiveData<GameMode>()
    private val difficultiesLiveData = MutableLiveData<List<Difficulty>>()
    private val gameModesLiveData = MutableLiveData<List<GameMode>>()
    val listToChooseFrom = MutableLiveData<List<String>>()
    val selectedListItem = MutableLiveData<String>()
    val messages = MutableLiveData<String>()
    private val questions = mutableListOf<ResultApiEntity>()
    private var currentQuestionIndex = 0
    private var selecting = NOTHING
    private var timerDisposable = Disposables.disposed()

    init {
        difficultiesLiveData.value = Difficulty.values().toList()
        gameModesLiveData.value = GameMode.values().toList()
        currentCategory.value = anyCategory
        currentDifficulty.value = Difficulty.ANY
        currentMode.value = GameMode.ANY
        screen.value = GameScreen.MAIN_MENU
    }

    fun onPlayClicked(amount: Int) {
        numberOfCorrectAnswers.value = 0
        numberOfQuestions.value = amount
        val client = OkHttpClient.Builder()
        client.addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })

        val retrofitTrivia = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .baseUrl(CategoryService.BASE_URL)
            .build()

        val apiTrivia = retrofitTrivia.create(TriviaService::class.java)
        val categoryId =
            if (currentCategory.value?.id == -1)
                ""
            else
                currentCategory.value?.id?.toString() ?: ""

        val difficulty = when (currentDifficulty.value ?: Difficulty.ANY) {
            Difficulty.ANY -> ""
            Difficulty.EASY -> "easy"
            Difficulty.MEDIUM -> "medium"
            Difficulty.HARD -> "hard"
        }

        val type = when (currentMode.value ?: GameMode.ANY) {
            GameMode.ANY -> ""
            GameMode.MULTIPLE -> "multiple"
            GameMode.BOOLEAN -> "boolean"
        }

        apiTrivia
            .getQuestions(amount, categoryId, difficulty, type)
            .enqueue(object : Callback<TriviaApiEntity> {
                override fun onFailure(call: Call<TriviaApiEntity>, t: Throwable) {
                    Log.d("TAG", "Failed")
                }

                override fun onResponse(
                    call: Call<TriviaApiEntity>,
                    response: Response<TriviaApiEntity>
                ) {
                    Log.d("TAG", response.body().toString())

                    Log.d("TAG", "modifiedResults")
                    val results = response.body()?.results?.toList()

                    questions.clear()
                    questions.addAll(results ?: listOf())
                    currentQuestionIndex = 0
                    nextQuestion()
                }
            })
        screen.value = GameScreen.GAME
    }

    fun nextQuestion() {
        questions
            .getOrNull(currentQuestionIndex)
            ?.let {
                resetTimer()
                val setup = QuestionSetup(it, currentQuestionIndex, questions.size)
                questionSetupLiveData.value = setup
            }
            ?: finishGame()
    }

    private fun finishGame() {
        screen.value = GameScreen.FINISH
        currentQuestionIndex = 0
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
                val list = body.triviaCategories.toMutableList()
                list.add(0, anyCategory)
                categoriesLiveData.value = list

                Log.d("TAG", "$body")
            }
        })
    }

    fun onListItemClicked(item: String) {
        screen.value = GameScreen.MAIN_MENU

        when (selecting) {
            NOTHING -> TODO()

            CATEGORY -> {
                val allCategories = categoriesLiveData.value ?: listOf()
                val newlySelected = allCategories.firstOrNull { it.name == item }
                currentCategory.value = newlySelected ?: anyCategory
            }

            DIFFICULTY -> {
                val allDifficulties = difficultiesLiveData.value ?: listOf()
                val newlySelected = allDifficulties.firstOrNull { it.displayableName == item }
                currentDifficulty.value = newlySelected ?: Difficulty.ANY
            }

            GAME_MODE -> {
                val allGameModes = gameModesLiveData.value ?: listOf()
                val newlySelected = allGameModes.firstOrNull { it.displayableName == item }
                currentMode.value = newlySelected ?: GameMode.ANY
            }
        }
    }

    private fun resetTimer() {
        timerDisposable.dispose()

        timerDisposable = Flowable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val remaining = THIRTY_SECONDS - it
                    if (remaining > -1) {
                        secondsRemaining.value = remaining
                    } else {
                        nextQuestion()
                    }
                },
                { it.printStackTrace() }
            )
    }

    fun onCategoryChooserClick() {
        screen.value = GameScreen.LIST
        selecting = CATEGORY
        listToChooseFrom.value = categoriesLiveData.value?.map { it.name }
        selectedListItem.value = currentCategory.value?.name
    }

    fun onDifficultyChooserClick() {
        screen.value = GameScreen.LIST
        selecting = DIFFICULTY
        listToChooseFrom.value = difficultiesLiveData.value?.map { it.displayableName }
        selectedListItem.value = currentDifficulty.value?.displayableName
    }

    fun onGameModeChooserClick() {
        screen.value = GameScreen.LIST
        selecting = GAME_MODE
        listToChooseFrom.value = gameModesLiveData.value?.map { it.displayableName }
        selectedListItem.value = currentMode.value?.displayableName
    }

    fun submitAnswer(answer: String) {
        val correctAnswer = questions
            .getOrNull(currentQuestionIndex++)?.correctAnswer
        if (correctAnswer == answer) {
            numberOfCorrectAnswers.value = numberOfCorrectAnswers.value?.inc() ?: 1
            messages.value = "Correct"
        } else {
            messages.value = "Wrong!"
        }

        nextQuestion()
    }

    fun setNumberOfQuestions(progress: Int) {
        numberOfQuestions.value = progress
    }

    data class QuestionSetup(
        val question: ResultApiEntity,
        val index: Int,
        val questionsAmount: Int
    )

    enum class Difficulty(val displayableName: String) {
        ANY("Any"), EASY("Easy"), MEDIUM("Medium"), HARD("Hard")
    }

    enum class GameMode(val displayableName: String) {
        ANY("Any"), MULTIPLE("Multiple choice"), BOOLEAN("True or false")
    }

    enum class GameScreen {
        MAIN_MENU, LIST, GAME, FINISH
    }

    enum class CurrentlySelecting {
        NOTHING, CATEGORY, DIFFICULTY, GAME_MODE
    }
}