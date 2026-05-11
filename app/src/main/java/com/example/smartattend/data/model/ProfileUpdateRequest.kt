package com.example.smartattend.data.model

data class ProfileUpdateRequest(
    val requestId: String = "",

    val requesterUid: String = "",
    val requesterRole: String = "", // employee or hr
    val requesterName: String = "",
    val requesterEmail: String = "",

    val employeeId: String = "",

    val targetApproverRole: String = "", // hr or admin

    val requestedFullName: String = "",
    val requestedPhone: String = "",
    val requestedGender: String = "",
    val requestedDob: String = "",
    val requestedAddress: String = "",
    val requestedEmergencyContact: String = "",
    val requestedPhotoUrl: String = "",

    val status: String = "pending", // pending, approved, rejected

    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L,
    val reviewedBy: String = "",
    val rejectReason: String = ""
)