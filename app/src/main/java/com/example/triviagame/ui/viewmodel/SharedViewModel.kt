package com.example.triviagame.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.triviagame.data.remote.ResultApiEntity
import com.example.triviagame.data.remote.TriviaCategoryApiEntity
import com.example.triviagame.data.repository.TriviaRepository
import com.example.triviagame.ui.viewmodel.SharedViewModel.CurrentlySelecting.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SharedViewModel : ViewModel() {

    companion object {
        private const val THIRTY_SECONDS = 30L
    }

    private val triviaRepository = TriviaRepository

    val currentCategory = MutableLiveData<TriviaCategoryApiEntity>()
    val questionSetupLiveData = MutableLiveData<QuestionSetup>()
    val listToChooseFrom = MutableLiveData<List<String>>()
    val currentDifficulty = MutableLiveData<Difficulty>()
    val numberOfCorrectAnswers = MutableLiveData<Int>()
    val selectedListItem = MutableLiveData<String>()
    val numberOfQuestions = MutableLiveData<Int>()
    val secondsRemaining = MutableLiveData<Long>()
    val currentMode = MutableLiveData<GameMode>()
    val screen = MutableLiveData<GameScreen>()
    val messages = MutableLiveData<String>()

    private val gameModesLiveData = MutableLiveData<List<GameMode>>()
    private val difficultiesLiveData = MutableLiveData<List<Difficulty>>()
    private val categoriesLiveData = MutableLiveData<List<TriviaCategoryApiEntity>>()

    private val questions = mutableListOf<ResultApiEntity>()
    private val anyCategory = TriviaCategoryApiEntity(-1, "Any")
    private var selecting = NOTHING
    private var currentQuestionIndex = 0

    private val disposables = CompositeDisposable()
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

        disposables.add(
            triviaRepository
                .getQuestions(amount, categoryId, difficulty, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val results = it.results.toList()

                    questions.clear()
                    questions.addAll(results)
                    currentQuestionIndex = 0
                    nextQuestion()
                }, {
                    it.printStackTrace()
                })
        )

        screen.value = GameScreen.GAME
    }

    private fun nextQuestion() {
        questions
            .getOrNull(currentQuestionIndex)
            ?.let {
                resetTimer()
                val setup =
                    QuestionSetup(
                        it,
                        currentQuestionIndex,
                        questions.size
                    )
                questionSetupLiveData.value = setup
            }
            ?: finishGame()
    }

    private fun finishGame() {
        screen.value = GameScreen.FINISH
        currentQuestionIndex = 0
    }

    fun fetchAllCategories() {
        disposables.add(
            triviaRepository.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val list = it.triviaCategories.toMutableList()
                    list.add(0, anyCategory)
                    categoriesLiveData.value = list
                }, {
                    it.printStackTrace()
                })
        )
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