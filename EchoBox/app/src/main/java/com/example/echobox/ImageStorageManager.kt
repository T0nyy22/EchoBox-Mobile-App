package com.example.echobox

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class ImageStorageManager(context: Context) {

    private val appContext = context.applicationContext

    private val photosDirectory =
        File(appContext.filesDir, "photos")

    init {
        if (!photosDirectory.exists()) {
            photosDirectory.mkdirs()
        }
    }

    // Copy selected image into internal storage
    fun saveImage(uriString: String): String {

        val uri = Uri.parse(uriString)

        val mimeType = appContext.contentResolver.getType(uri)

        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }

        val imageFile = File(
            photosDirectory,
            "image_${UUID.randomUUID()}.$extension"
        )

        try {
            val inputStream =
                appContext.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot open selected image")

            inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return imageFile.absolutePath

        } catch (e: Exception) {
            imageFile.delete()
            throw e
        }
    }

    // Delete image from internal storage
    fun deleteImage(imagePath: String?) {

        if (imagePath.isNullOrBlank()) return

        try {
            val directory = photosDirectory.canonicalFile
            val imageFile = File(imagePath).canonicalFile

            val directoryPath =
                directory.absolutePath + File.separator

            if (imageFile.absolutePath.startsWith(directoryPath)) {
                imageFile.delete()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}