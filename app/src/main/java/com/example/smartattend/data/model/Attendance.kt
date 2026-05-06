package com.example.smartattend.data.model

data class Attendance(
    val attendanceId: String = "",
    val employeeUid: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val workplaceId: String = "",
    val workplaceName: String = "",
    val date: String = "",
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val status: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val checkOutLatitude: Double = 0.0,
    val checkOutLongitude: Double = 0.0,
    val distanceMeter: Double = 0.0,
    val checkOutDistanceMeter: Double = 0.0,
    val isMockLocation: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)