package com.readrops.app.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object FileUtils {

    @JvmStatic
    fun writeDownloadFile(context: Context, fileName: String, mimeType: String, listener: (OutputStream) -> Unit): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            writeFileApi29(context, fileName, mimeType, listener)
        else
            writeFileApi28(fileName, listener)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeFileApi29(context: Context, fileName: String, mimeType: String, listener: (OutputStream) -> Unit): String {
        val resolver = context.contentResolver
        val downloadsUri = MediaStore.Downloads
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val fileDetails = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.IS_PENDING, 1)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
        }

        val contentUri = resolver.insert(downloadsUri, fileDetails)

        resolver.openFileDescriptor(contentUri!!, "w", null).use { pfd ->
            val outputStream = FileOutputStream(pfd?.fileDescriptor!!)

            try {
                listener(outputStream)
            } catch (e: Exception) {
                throw e
            } finally {
                outputStream.flush()
                outputStream.close()
            }
        }

        fileDetails.clear()
        fileDetails.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(contentUri, fileDetails, null, null)

        return contentUri.path!!
    }

    private fun writeFileApi28(fileName: String, listener: (OutputStream) -> Unit): String {
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val file = File(filePath, fileName)

        val outputStream = FileOutputStream(file)
        listener(outputStream)

        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }
}