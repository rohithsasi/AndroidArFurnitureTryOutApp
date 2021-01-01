package com.example.myfirstarfurnitureapp

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.sceneform.ArSceneView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PhotoSaver(private val activity: Activity) {

    private fun generateFilename(): String? {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.absolutePath +
                "/TryOutFurniture/${date}_screenshot.jpg"
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {

            put(MediaStore.MediaColumns.DISPLAY_NAME, "${date}_ar_screenshot.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/furniture")
            }
        }

        val uri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        activity.contentResolver.openOutputStream(uri ?: return).use { output ->

            output?.let {
                try {
                    saveDataToGallery(bitmap, it)
                } catch (e: IOException) {
                    Toast.makeText(activity, "Failed to save bitmap to gallery.", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun saveDataToGallery(bmp: Bitmap, outputStream: OutputStream) {
        val outputData = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputData)
        outputData.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
    }

    private fun saveBitmapToGallery(bmp: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile.exists()) {
            out.parentFile.mkdirs()
        }
        try {
            val outputStream = FileOutputStream(filename)
            saveDataToGallery(bmp, outputStream)
            MediaScannerConnection.scanFile(activity, arrayOf(filename), null, null)
        } catch (ex: Exception) {
            Toast.makeText(activity, "Failed to save bitmap to gallery.", Toast.LENGTH_LONG).show()
        }
    }

    fun takePhoto(arSceneView: ArSceneView) {
        val bitmap =
            Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        val workerThread = HandlerThread("Bitmap creator from sceneview pixels")
        workerThread.start()

        PixelCopy.request(arSceneView, bitmap, { res ->
            if (res == PixelCopy.SUCCESS) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    val fileName = generateFilename()
                    fileName?.let { saveBitmapToGallery(bitmap, filename = it) }
                } else {
                    saveBitmapToGallery(bitmap)
                }
                activity.runOnUiThread {
                    Toast.makeText(activity, "Successfully took photo!", Toast.LENGTH_LONG).show()
                }
            } else {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Failed to take photo: Version less than Android P",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            workerThread.quitSafely()
        }, Handler(workerThread.looper))
    }
}