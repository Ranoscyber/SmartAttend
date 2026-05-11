package com.example.smartattend.ui.hr

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartattend.data.model.ProfileUpdateRequest

@Composable
fun HrProfileRequestsContent(
    requests: List<ProfileUpdateRequest>,
    isLoading: Boolean,
    onApprove: (ProfileUpdateRequest) -> Unit,
    onReject: (String, String) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyProfileRequestCard()
        return
    }

    val pendingRequests = requests.filter { it.status == "pending" }
    val reviewedRequests = requests.filter { it.status != "pending" }

    if (pendingRequests.isNotEmpty()) {
        Text(
            text = "Pending Requests",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        pendingRequests.forEach { request ->
            ProfileRequestItem(
                request = request,
                isLoading = isLoading,
                onApprove = onApprove,
                onReject = onReject
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }

    if (reviewedRequests.isNotEmpty()) {
        Text(
            text = "Reviewed Requests",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        reviewedRequests.forEach { request ->
            ProfileRequestItem(
                request = request,
                isLoading = isLoading,
                onApprove = onApprove,
                onReject = onReject
            )
        }
    }
}

@Composable
private fun ProfileRequestItem(
    request: ProfileUpdateRequest,
    isLoading: Boolean,
    onApprove: (ProfileUpdateRequest) -> Unit,
    onReject: (String, String) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RequestAvatar(photoUrl = request.requestedPhotoUrl)

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = request.requesterName.ifBlank { "Employee Request" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = request.requesterEmail.ifBlank { "-" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Text(
                        text = request.employeeId.ifBlank { "-" },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                StatusChip(status = request.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Requested Changes",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            RequestInfoRow("Full Name", request.requestedFullName.ifBlank { "-" })
            RequestInfoRow("Phone", request.requestedPhone.ifBlank { "-" })
            RequestInfoRow("Gender", request.requestedGender.ifBlank { "-" })
            RequestInfoRow("Date of Birth", request.requestedDob.ifBlank { "-" })
            RequestInfoRow("Address", request.requestedAddress.ifBlank { "-" })
            RequestInfoRow("Emergency Contact", request.requestedEmergencyContact.ifBlank { "-" })

            if (request.status == "rejected" && request.rejectReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Reject reason: ${request.rejectReason}",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (request.status == "pending") {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showRejectDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Reject"
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text("Reject")
                    }

                    Button(
                        onClick = {
                            onApprove(request)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Approve"
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text("Approve")
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        RejectReasonDialog(
            onDismiss = {
                showRejectDialog = false
            },
            onConfirm = { reason ->
                showRejectDialog = false
                onReject(request.requestId, reason)
            }
        )
    }
}

@Composable
private fun RequestAvatar(
    photoUrl: String
) {
    Surface(
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Requested Profile Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: String
) {
    val label = status.ifBlank { "pending" }

    AssistChip(
        onClick = {},
        label = {
            Text(label)
        },
        leadingIcon = {
            when (label) {
                "approved" -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Approved"
                    )
                }

                "rejected" -> {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Rejected"
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Rounded.HourglassTop,
                        contentDescription = "Pending"
                    )
                }
            }
        }
    )
}

@Composable
private fun RequestInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Text(
            text = value,
            modifier = Modifier.weight(1.3f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EmptyProfileRequestCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "No profile update requests",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Employee profile update requests will appear here.",
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RejectReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Request?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Please enter a reason for rejection.")

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Reason")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "Rejected by HR" })
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}