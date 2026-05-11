package com.example.smartattend.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.model.ProfileUpdateRequest
import com.example.smartattend.data.remote.CloudinaryConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class EmployeeRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val cloudinaryUploadRepository = CloudinaryUploadRepository()

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

    suspend fun createProfileUpdateRequest(
        context: Context,
        employee: Employee,
        fullName: String,
        phone: String,
        gender: String,
        dob: String,
        address: String,
        emergencyContact: String,
        imageUri: Uri?
    ): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Employee user not found"))

            val existingPendingSnapshot = database
                .child("profile_update_requests")
                .orderByChild("requesterUid")
                .equalTo(uid)
                .get()
                .await()

            val hasPendingRequest = existingPendingSnapshot.children.any {
                it.child("status").value?.toString() == "pending"
            }

            if (hasPendingRequest) {
                return Result.failure(Exception("You already have a pending profile update request"))
            }

            val photoUrl = if (imageUri != null) {
                val uploadResult = cloudinaryUploadRepository.uploadImage(
                    context = context,
                    imageUri = imageUri,
                    folder = CloudinaryConfig.EMPLOYEE_PROFILE_FOLDER
                )

                uploadResult.getOrElse { error ->
                    return Result.failure(error)
                }
            } else {
                employee.photoUrl
            }

            val requestRef = database
                .child("profile_update_requests")
                .push()

            val requestId = requestRef.key
                ?: return Result.failure(Exception("Failed to create request ID"))

            val request = ProfileUpdateRequest(
                requestId = requestId,
                requesterUid = uid,
                requesterRole = "employee",
                requesterName = employee.fullName,
                requesterEmail = employee.email,
                employeeId = employee.employeeId,
                targetApproverRole = "hr",

                requestedFullName = fullName.trim(),
                requestedPhone = phone.trim(),
                requestedGender = gender.trim(),
                requestedDob = dob.trim(),
                requestedAddress = address.trim(),
                requestedEmergencyContact = emergencyContact.trim(),
                requestedPhotoUrl = photoUrl,

                status = "pending",
                createdAt = System.currentTimeMillis()
            )

            requestRef.setValue(request).await()

            Result.success(requestId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}