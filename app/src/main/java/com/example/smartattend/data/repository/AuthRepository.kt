package com.example.smartattend.data.repository

import com.example.smartattend.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun login(email: String, password: String): Result<AppUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()

            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("User UID not found"))

            val snapshot = database
                .child("users")
                .child(uid)
                .get()
                .await()

            val user = snapshot.getValue(AppUser::class.java)
                ?: return Result.failure(Exception("User data not found in database"))

            if (user.status != "active") {
                return Result.failure(Exception("Your account is not active"))
            }

            Result.success(user.copy(uid = uid))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserData(): Result<AppUser> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No logged-in user"))

            val snapshot = database
                .child("users")
                .child(uid)
                .get()
                .await()

            val user = snapshot.getValue(AppUser::class.java)
                ?: return Result.failure(Exception("User data not found"))

            Result.success(user.copy(uid = uid))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}