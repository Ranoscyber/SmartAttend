package com.example.smartattend.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartattend.data.model.AppUser
import com.example.smartattend.data.model.HRProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.model.FakeLocationAlert
import com.example.smartattend.data.model.SalaryReport
import com.example.smartattend.util.DateTimeUtil


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

    suspend fun getSalaryReports(): Result<List<SalaryReport>> {
        return try {
            val currentMonth = DateTimeUtil.currentMonth()

            val employeesSnapshot = database
                .child("employees")
                .get()
                .await()

            val attendanceSnapshot = database
                .child("attendance")
                .get()
                .await()

            val employees = employeesSnapshot.children.mapNotNull {
                it.getValue(Employee::class.java)
            }.filter {
                it.status == "active"
            }

            val attendanceList = attendanceSnapshot.children.mapNotNull {
                it.getValue(Attendance::class.java)
            }.filter {
                it.date.startsWith(currentMonth)
            }

            val reports = employees.map { employee ->
                val employeeAttendances = attendanceList.filter {
                    it.employeeId == employee.employeeId
                }

                val attendedDays = employeeAttendances
                    .map { it.date }
                    .distinct()
                    .size

                val lateDays = employeeAttendances.count {
                    it.status == "Late"
                }

                val workDays = if (employee.workDaysPerMonth > 0) {
                    employee.workDaysPerMonth
                } else {
                    30
                }

                val absentDays = (workDays - attendedDays).coerceAtLeast(0)

                val dailySalary = if (workDays > 0) {
                    employee.baseSalary / workDays
                } else {
                    0.0
                }

                val deduction = absentDays * dailySalary
                val finalSalary = (employee.baseSalary - deduction).coerceAtLeast(0.0)

                SalaryReport(
                    employeeId = employee.employeeId,
                    employeeName = employee.fullName,
                    departmentName = employee.departmentName,
                    position = employee.position,
                    month = currentMonth,
                    baseSalary = employee.baseSalary,
                    workDaysPerMonth = workDays,
                    attendedDays = attendedDays,
                    absentDays = absentDays,
                    lateDays = lateDays,
                    dailySalary = dailySalary,
                    deduction = deduction,
                    finalSalary = finalSalary
                )
            }.sortedBy {
                it.employeeId
            }

            Result.success(reports)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHrProfiles(): Result<List<HRProfile>> {
        return try {
            val snapshot = database
                .child("hr_profiles")
                .get()
                .await()

            val hrProfiles = snapshot.children.mapNotNull {
                it.getValue(HRProfile::class.java)
            }.sortedBy {
                it.fullName
            }

            Result.success(hrProfiles)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}