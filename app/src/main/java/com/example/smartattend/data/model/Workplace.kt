package com.example.smartattend.data.model

data class Workplace(
    val workplaceId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val allowedRadius: Double = 100.0,
    val startTime: String = "08:00",
    val lateAfterTime: String = "08:15",
    val qrCodeValue: String = "",
    val status: String = "active",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)