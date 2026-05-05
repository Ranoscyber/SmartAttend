package com.example.smartattend.data.model

data class ProfileUpdateRequest(
    val requestId: String = "",
    val requesterUid: String = "",
    val requesterRole: String = "",
    val requesterName: String = "",
    val targetNode: String = "",
    val targetId: String = "",
    val fieldName: String = "",
    val oldValue: String = "",
    val newValue: String = "",
    val reason: String = "",
    val status: String = "pending",
    val approverRole: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedBy: String = "",
    val reviewedAt: Long = 0L,
    val reviewNote: String = ""
)