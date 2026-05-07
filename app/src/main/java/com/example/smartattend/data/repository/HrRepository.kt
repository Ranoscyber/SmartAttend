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
import com.example.smartattend.data.model.Workplace
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.FakeLocationAlert

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

    suspend fun saveWorkplace(
        name: String,
        latitude: Double,
        longitude: Double,
        allowedRadius: Double,
        startTime: String,
        lateAfterTime: String
    ): Result<String> {
        return try {
            val currentHrUid = auth.currentUser?.uid
                ?: return Result.failure(Exception("HR user not found"))

            val activeWorkplaceIdSnapshot = database
                .child("app_settings")
                .child("activeWorkplaceId")
                .get()
                .await()

            val existingWorkplaceId = activeWorkplaceIdSnapshot.value?.toString()

            val workplaceId: String
            val qrCodeValue: String
            val createdAt: Long

            if (!existingWorkplaceId.isNullOrBlank()) {
                workplaceId = existingWorkplaceId

                val oldSnapshot = database
                    .child("workplaces")
                    .child(workplaceId)
                    .get()
                    .await()

                qrCodeValue = oldSnapshot.child("qrCodeValue").value?.toString()
                    ?: "SMARTATTEND_$workplaceId"

                createdAt = oldSnapshot.child("createdAt").getValue(Long::class.java)
                    ?: System.currentTimeMillis()
            } else {
                val counterSnapshot = database
                    .child("counters")
                    .child("workplaceCounter")
                    .get()
                    .await()

                val currentCounter = counterSnapshot.getValue(Int::class.java) ?: 1

                workplaceId = generateWorkplaceId(currentCounter)
                qrCodeValue = "SMARTATTEND_$workplaceId"
                createdAt = System.currentTimeMillis()

                database
                    .child("counters")
                    .child("workplaceCounter")
                    .setValue(currentCounter + 1)
                    .await()
            }

            val workplace = Workplace(
                workplaceId = workplaceId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                allowedRadius = allowedRadius,
                startTime = startTime,
                lateAfterTime = lateAfterTime,
                qrCodeValue = qrCodeValue,
                status = "active",
                createdBy = currentHrUid,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis()
            )

            val updates = hashMapOf<String, Any>(
                "workplaces/$workplaceId" to workplace,
                "app_settings/activeWorkplaceId" to workplaceId
            )

            database.updateChildren(updates).await()

            Result.success(workplaceId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveWorkplace(): Result<Workplace?> {
        return try {
            val activeWorkplaceIdSnapshot = database
                .child("app_settings")
                .child("activeWorkplaceId")
                .get()
                .await()

            val workplaceId = activeWorkplaceIdSnapshot.value?.toString()

            if (workplaceId.isNullOrBlank()) {
                return Result.success(null)
            }

            val workplaceSnapshot = database
                .child("workplaces")
                .child(workplaceId)
                .get()
                .await()

            val workplace = workplaceSnapshot.getValue(Workplace::class.java)

            Result.success(workplace)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateWorkplaceId(counter: Int): String {
        return "WP" + counter.toString().padStart(3, '0')
    }

    suspend fun getAttendanceReports(): Result<List<Attendance>> {
        return try {
            val snapshot = database
                .child("attendance")
                .get()
                .await()

            val reports = snapshot.children.mapNotNull {
                it.getValue(Attendance::class.java)
            }.sortedWith(
                compareByDescending<Attendance> { it.date }
                    .thenByDescending { it.createdAt }
            )

            Result.success(reports)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFakeLocationAlerts(): Result<List<FakeLocationAlert>> {
        return try {
            val snapshot = database
                .child("fake_location_alerts")
                .get()
                .await()

            val alerts = snapshot.children.mapNotNull {
                it.getValue(FakeLocationAlert::class.java)
            }.sortedByDescending {
                it.createdAt
            }

            Result.success(alerts)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markFakeLocationAlertAsRead(alertId: String): Result<Unit> {
        return try {
            database
                .child("fake_location_alerts")
                .child(alertId)
                .child("status")
                .setValue("read")
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}