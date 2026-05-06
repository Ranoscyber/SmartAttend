package com.example.smartattend.util

import android.location.Location
import android.os.Build

object LocationValidator {

    fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    fun calculateDistanceMeters(
        employeeLatitude: Double,
        employeeLongitude: Double,
        workplaceLatitude: Double,
        workplaceLongitude: Double
    ): Float {
        val result = FloatArray(1)

        Location.distanceBetween(
            employeeLatitude,
            employeeLongitude,
            workplaceLatitude,
            workplaceLongitude,
            result
        )

        return result[0]
    }
}