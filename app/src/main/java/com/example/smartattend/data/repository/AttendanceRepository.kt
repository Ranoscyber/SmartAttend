package com.example.smartattend.data.repository

import android.location.Location
import com.example.smartattend.data.model.Attendance
import com.example.smartattend.data.model.Employee
import com.example.smartattend.data.model.FakeLocationAlert
import com.example.smartattend.data.model.Workplace
import com.example.smartattend.util.DateTimeUtil
import com.example.smartattend.util.LocationValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AttendanceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun checkInWithQr(
        scannedQrValue: String,
        location: Location
    ): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Employee user not found"))

            val employee = getCurrentEmployee(uid)
            val workplace = getActiveWorkplace()
                ?: return Result.failure(Exception("No active workplace found"))

            if (LocationValidator.isMockLocation(location)) {
                saveFakeLocationAlert(
                    employee = employee,
                    workplace = workplace,
                    location = location
                )

                return Result.failure(
                    Exception("Fake GPS detected. Attendance denied and HR has been alerted.")
                )
            }

            if (scannedQrValue.trim() != workplace.qrCodeValue.trim()) {
                return Result.failure(Exception("Invalid workplace QR code"))
            }

            val distance = LocationValidator.calculateDistanceMeters(
                employeeLatitude = location.latitude,
                employeeLongitude = location.longitude,
                workplaceLatitude = workplace.latitude,
                workplaceLongitude = workplace.longitude
            )

            if (distance > workplace.allowedRadius) {
                return Result.failure(
                    Exception("You are not at the workplace. Distance: ${distance.toInt()} meters")
                )
            }

            val today = DateTimeUtil.todayDate()
            val attendanceId = "${employee.employeeId}_$today"

            val existingAttendance = database
                .child("attendance")
                .child(attendanceId)
                .get()
                .await()

            if (existingAttendance.exists()) {
                return Result.failure(Exception("You already checked in today"))
            }

            val checkInTime = DateTimeUtil.currentTime()
            val attendanceStatus = if (
                DateTimeUtil.isLate(checkInTime, workplace.lateAfterTime)
            ) {
                "Late"
            } else {
                "Present"
            }

            val attendance = Attendance(
                attendanceId = attendanceId,
                employeeUid = uid,
                employeeId = employee.employeeId,
                employeeName = employee.fullName,
                workplaceId = workplace.workplaceId,
                workplaceName = workplace.name,
                date = today,
                checkInTime = checkInTime,
                checkOutTime = "",
                status = attendanceStatus,
                latitude = location.latitude,
                longitude = location.longitude,
                distanceMeter = distance.toDouble(),
                isMockLocation = false,
                createdAt = DateTimeUtil.currentTimestamp()
            )

            database
                .child("attendance")
                .child(attendanceId)
                .setValue(attendance)
                .await()

            Result.success("Check-in successful: $attendanceStatus")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCurrentEmployee(uid: String): Employee {
        val userSnapshot = database
            .child("users")
            .child(uid)
            .get()
            .await()

        val employeeId = userSnapshot.child("employeeId").value?.toString()
            ?: throw Exception("Employee ID not found")

        if (employeeId.isBlank()) {
            throw Exception("Employee ID is empty")
        }

        val employeeSnapshot = database
            .child("employees")
            .child(employeeId)
            .get()
            .await()

        return employeeSnapshot.getValue(Employee::class.java)
            ?: throw Exception("Employee profile not found")
    }

    private suspend fun getActiveWorkplace(): Workplace? {
        val activeWorkplaceSnapshot = database
            .child("app_settings")
            .child("activeWorkplaceId")
            .get()
            .await()

        val workplaceId = activeWorkplaceSnapshot.value?.toString()

        if (workplaceId.isNullOrBlank()) {
            return null
        }

        val workplaceSnapshot = database
            .child("workplaces")
            .child(workplaceId)
            .get()
            .await()

        return workplaceSnapshot.getValue(Workplace::class.java)
    }

    private suspend fun saveFakeLocationAlert(
        employee: Employee,
        workplace: Workplace,
        location: Location
    ) {
        val alertRef = database
            .child("fake_location_alerts")
            .push()

        val alertId = alertRef.key ?: return

        val alert = FakeLocationAlert(
            alertId = alertId,
            employeeUid = employee.uid,
            employeeId = employee.employeeId,
            employeeName = employee.fullName,
            workplaceId = workplace.workplaceId,
            workplaceName = workplace.name,
            latitude = location.latitude,
            longitude = location.longitude,
            reason = "Mock/Fake GPS location detected during check-in",
            status = "unread",
            createdAt = DateTimeUtil.currentTimestamp()
        )

        alertRef.setValue(alert).await()
    }
}