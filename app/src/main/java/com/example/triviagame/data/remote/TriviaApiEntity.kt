package com.example.triviagame.data.remote

import com.google.gson.annotations.SerializedName

data class TriviaApiEntity(
    @SerializedName("response_code")
    val responseCode: Int,
    val results: List<ResultApiEntity>
)