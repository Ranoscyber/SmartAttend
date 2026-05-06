package com.example.smartattend.data.model

data class Department(
    val departmentId: String = "",
    val name: String = "",
    val description: String = "",
    val status: String = "active",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)