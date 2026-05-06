package com.example.smartattend.data.repository

import android.content.Context
import android.net.Uri
import com.example.smartattend.data.remote.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CloudinaryUploadRepository {

    private val client = OkHttpClient()

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = CloudinaryConfig.HR_PROFILE_FOLDER
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext Result.failure(Exception("Cannot open selected image"))

                val imageBytes = inputStream.use { it.readBytes() }

                val imageRequestBody = imageBytes.toRequestBody(
                    contentType = "image/*".toMediaTypeOrNull()
                )

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        name = "file",
                        filename = "profile.jpg",
                        body = imageRequestBody
                    )
                    .addFormDataPart(
                        name = "upload_preset",
                        value = CloudinaryConfig.UPLOAD_PRESET
                    )
                    .addFormDataPart(
                        name = "folder",
                        value = folder
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${CloudinaryConfig.CLOUD_NAME}/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                    return@withContext Result.failure(
                        Exception("Cloudinary upload failed: ${response.message}")
                    )
                }

                val json = JSONObject(responseBody)
                val imageUrl = json.getString("secure_url")

                Result.success(imageUrl)

            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}