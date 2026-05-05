package com.example.smartattend.data.model

data class HRProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)