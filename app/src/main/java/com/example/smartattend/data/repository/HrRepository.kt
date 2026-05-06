package com.example.smartattend.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartattend.data.model.AppUser
import com.example.smartattend.data.model.Department
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.remote.CloudinaryConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class HrRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val cloudinaryUploadRepository = CloudinaryUploadRepository()

    suspend fun createDepartment(
        name: String,
        description: String
    ): Result<String> {
        return try {
            val currentHrUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("HR user not found"))

            val counterSnapshot = database
                .child("counters")
                .child("departmentCounter")
                .get()
                .await()

            val currentCounter = counterSnapshot.getValue(Int::class.java) ?: 1
            val departmentId = generateDepartmentId(currentCounter)

            val department = Department(
                departmentId = departmentId,
                name = name,
                description = description,
                status = "active",
                createdBy = currentHrUid,
                createdAt = System.currentTimeMillis()
            )

            val updates = hashMapOf<String, Any>(
                "departments/$departmentId" to department,
                "counters/departmentCounter" to currentCounter + 1
            )

            database.updateChildren(updates).await()

            Result.success(departmentId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDepartments(): Result<List<Department>> {
        return try {
            val snapshot = database
                .child("departments")
                .get()
                .await()

            val departments = snapshot.children.mapNotNull {
                it.getValue(Department::class.java)
            }.filter {
                it.status == "active"
            }

            Result.success(departments)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createEmployee(
        context: Context,
        fullName: String,
        email: String,
        password: String,
        phone: String,
        gender: String,
        dob: String,
        address: String,
        emergencyContact: String,
        department: Department,
        position: String,
        employmentType: String,
        joinDate: String,
        baseSalary: Double,
        workDaysPerMonth: Int,
        imageUri: Uri?
    ): Result<String> {
        return try {
            val currentHrUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("HR user not found"))

            val counterSnapshot = database
                .child("counters")
                .child("employeeCounter")
                .get()
                .await()

            val currentCounter = counterSnapshot.getValue(Int::class.java) ?: 1
            val employeeId = generateEmployeeId(currentCounter)

            /*
             * Important:
             * Use secondary Firebase Auth so current HR session does NOT switch to employee.
             */
            val secondaryAuth = getSecondaryAuth(context)

            val authResult = secondaryAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val employeeUid = authResult.user?.uid
                ?: return Result.failure(Exception("Failed to get employee UID"))

            secondaryAuth.signOut()

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
                ""
            }

            val user = AppUser(
                uid = employeeUid,
                name = fullName,
                email = email,
                role = "employee",
                status = "active",
                employeeId = employeeId
            )

            val employee = Employee(
                uid = employeeUid,
                employeeId = employeeId,
                fullName = fullName,
                email = email,
                phone = phone,
                gender = gender,
                dob = dob,
                address = address,
                emergencyContact = emergencyContact,
                departmentId = department.departmentId,
                departmentName = department.name,
                position = position,
                employmentType = employmentType,
                joinDate = joinDate,
                baseSalary = baseSalary,
                workDaysPerMonth = workDaysPerMonth,
                photoUrl = photoUrl,
                status = "active",
                createdBy = currentHrUid,
                createdAt = System.currentTimeMillis()
            )

            val updates = hashMapOf<String, Any>(
                "users/$employeeUid" to user,
                "employees/$employeeId" to employee,
                "counters/employeeCounter" to currentCounter + 1
            )

            database.updateChildren(updates).await()

            Result.success(employeeId)

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

    private fun getSecondaryAuth(context: Context): FirebaseAuth {
        val appName = "SecondaryAuthApp"

        val secondaryApp = try {
            FirebaseApp.getInstance(appName)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                context,
                FirebaseApp.getInstance().options,
                appName
            ) ?: throw Exception("Failed to initialize secondary Firebase app")
        }

        return FirebaseAuth.getInstance(secondaryApp)
    }

    private fun generateDepartmentId(counter: Int): String {
        return "DEP" + counter.toString().padStart(3, '0')
    }

    private fun generateEmployeeId(counter: Int): String {
        return "EMP" + counter.toString().padStart(3, '0')
    }
}