package com.example.smartattend.data.model

data class FakeLocationAlert(
    val alertId: String = "",
    val employeeUid: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val workplaceId: String = "",
    val workplaceName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val reason: String = "",
    val status: String = "unread",
    val createdAt: Long = System.currentTimeMillis()
)