package com.example.triviagame.data.remote

import com.google.gson.annotations.SerializedName

data class TriviaCategoryApiEntity(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

