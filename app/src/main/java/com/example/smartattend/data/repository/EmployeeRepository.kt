package com.example.smartattend.data.repository

import com.example.smartattend.data.model.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class EmployeeRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun getCurrentEmployee(): Result<Employee> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Employee user not found"))

            val userSnapshot = database
                .child("users")
                .child(uid)
                .get()
                .await()

            val employeeId = userSnapshot.child("employeeId").value?.toString()
                ?: return Result.failure(Exception("Employee ID not found"))

            if (employeeId.isBlank()) {
                return Result.failure(Exception("Employee ID is empty"))
            }

            val employeeSnapshot = database
                .child("employees")
                .child(employeeId)
                .get()
                .await()

            val employee = employeeSnapshot.getValue(Employee::class.java)
                ?: return Result.failure(Exception("Employee profile not found"))

            Result.success(employee)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}