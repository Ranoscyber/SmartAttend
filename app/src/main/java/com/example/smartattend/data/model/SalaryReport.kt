package com.example.smartattend.data.model

data class SalaryReport(
    val employeeId: String = "",
    val employeeName: String = "",
    val departmentName: String = "",
    val position: String = "",
    val month: String = "",
    val baseSalary: Double = 0.0,
    val workDaysPerMonth: Int = 30,
    val attendedDays: Int = 0,
    val absentDays: Int = 0,
    val lateDays: Int = 0,
    val dailySalary: Double = 0.0,
    val deduction: Double = 0.0,
    val finalSalary: Double = 0.0
)