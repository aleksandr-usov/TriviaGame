package com.example.triviagame.data.remote

import com.google.gson.annotations.SerializedName

data class CategoryApiEntity(
    @SerializedName("trivia_categories")
    val triviaCategories: List<TriviaCategoryApiEntity>
)