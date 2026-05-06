package com.example.smartattend.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartattend.data.model.AppUser
import com.example.smartattend.data.model.HRProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val cloudinaryUploadRepository = CloudinaryUploadRepository()

    suspend fun createHrAccount(
        context: Context,
        fullName: String,
        email: String,
        password: String,
        phone: String,
        imageUri: Uri?
    ): Result<String> {
        return try {
            val authResult = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val hrUid = authResult.user?.uid
                ?: return Result.failure(Exception("Failed to get HR UID"))

            val photoUrl = if (imageUri != null) {
                val uploadResult = cloudinaryUploadRepository.uploadImage(
                    context = context,
                    imageUri = imageUri
                )

                uploadResult.getOrElse { error ->
                    return Result.failure(error)
                }
            } else {
                ""
            }

            val user = AppUser(
                uid = hrUid,
                name = fullName,
                email = email,
                role = "hr",
                status = "active",
                employeeId = ""
            )

            val hrProfile = HRProfile(
                uid = hrUid,
                fullName = fullName,
                email = email,
                phone = phone,
                gender = "",
                address = "",
                photoUrl = photoUrl,
                status = "active"
            )

            val updates = hashMapOf<String, Any>(
                "users/$hrUid" to user,
                "hr_profiles/$hrUid" to hrProfile
            )

            database.updateChildren(updates).await()

            /*
             * Firebase client SDK signs in as the newly created HR account.
             * For this free/student version, we sign out and ask Admin to login again.
             */
            auth.signOut()

            Result.success(hrUid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHrCount(): Result<Int> {
        return try {
            val snapshot = database.child("users").get().await()

            var count = 0

            snapshot.children.forEach { child ->
                val role = child.child("role").value?.toString()
                if (role == "hr") {
                    count++
                }
            }

            Result.success(count)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmployeeCount(): Result<Int> {
        return try {
            val snapshot = database.child("employees").get().await()
            Result.success(snapshot.childrenCount.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}