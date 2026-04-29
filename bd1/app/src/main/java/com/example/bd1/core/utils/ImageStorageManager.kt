package com.example.bd1

import android.content.Context
import android.net.Uri
import java.io.File

class ImageStorageManager(private val context: Context) {

    fun persistImage(uri: Uri): String? {
        return runCatching {
            val imagesDir = File(context.filesDir, "product_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val extension = guessExtension(uri)
            val file = File(imagesDir, "img_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            Uri.fromFile(file).toString()
        }.getOrNull()
    }

    private fun guessExtension(uri: Uri): String {
        val mime = context.contentResolver.getType(uri).orEmpty()
        return when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
    }
}