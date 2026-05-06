package com.example.smartattend.data.model

data class Employee(
    val uid: String = "",
    val employeeId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val dob: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val departmentId: String = "",
    val departmentName: String = "",
    val position: String = "",
    val employmentType: String = "",
    val joinDate: String = "",
    val baseSalary: Double = 0.0,
    val workDaysPerMonth: Int = 30,
    val photoUrl: String = "",
    val status: String = "active",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)