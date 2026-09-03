package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageHelper {

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val filename = "qc_damage_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val directory = File(context.filesDir, "damage_photos")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val filename = "qc_damage_picked_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val directory = File(context.filesDir, "damage_photos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)
            FileOutputStream(file).use { out ->
                inputStream?.copyTo(out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
