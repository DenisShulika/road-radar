package com.denisshulika.road_radar.model

data class UserData(
    val uid: String,
    val email: String,
    val password: String,
    val name: String,
    val phoneNumber: String,
    val photoUrl: String? = null
)