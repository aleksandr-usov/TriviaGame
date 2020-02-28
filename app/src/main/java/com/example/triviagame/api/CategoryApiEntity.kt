package com.example.triviagame.api


import com.google.gson.annotations.SerializedName

data class CategoryApiEntity(
    @SerializedName("trivia_categories")
    val triviaCategories: List<TriviaCategoryApiEntity>
)