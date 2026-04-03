package com.example.loginapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RandomUserResponse(
    @SerializedName("results")
    val results: List<RandomUserResult>
)

data class RandomUserResult(
    @SerializedName("name")
    val name: Name,
    @SerializedName("email")
    val email: String,
    @SerializedName("picture")
    val picture: Picture,
    @SerializedName("location")
    val location: Location
)

data class Name(
    @SerializedName("first")
    val first: String,
    @SerializedName("last")
    val last: String
)

data class Picture(
    @SerializedName("large")
    val large: String,
    @SerializedName("medium")
    val medium: String,
    @SerializedName("thumbnail")
    val thumbnail: String
)

data class Location(
    @SerializedName("city")
    val city: String,
    @SerializedName("country")
    val country: String
)
