package com.wayneyong.furniture_try_out_ar.furnituretryout

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ar.sceneform.ArSceneView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoSaver(private val activity: Activity) {

    private fun generateFilename(): String? {
        //get current time
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.absolutePath +
                "/TryOutFurniture/${date}_screenshot.jpg"
    }


    //for api 29
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${date}_screenshot.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/TryOutFurniture")
        }

        //path to image
        val uri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        //if null just return
        activity.contentResolver.openOutputStream(uri ?: return).use { outputStream ->
            outputStream?.let {
                try {
                    saveDataToGalley(bitmap, outputStream)

                } catch (e: Exception) {
                    Toast.makeText(
                        activity,
                        "Failed to save bitmap to gallery.",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.e("PhotoSaver", "Failed to save bitmap to gallery.")
                }
            }
        }
    }

    private fun saveBitmapToGalley(bitmap: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile.exists()) {
            out.parentFile.mkdir()
        }
        try {
            val outputStream = FileOutputStream(filename)
            saveDataToGalley(bitmap, outputStream)
            MediaScannerConnection.scanFile(activity, arrayOf(filename), null, null)

        } catch (e: Exception) {
            Toast.makeText(activity, "Failed to save bitmap to gallery.", Toast.LENGTH_SHORT).show()
            Log.e("PhotoSaver", "Failed to save bitmap to gallery.")
        }
    }

    private fun saveDataToGalley(bitmap: Bitmap, outputStream: OutputStream) {
        val outputData = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputData)

        outputData.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()


    }

    fun takePhoto(arSceneView: ArSceneView) {
        val bitmap =
            Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)

        val handlerThread = HandlerThread("PixelCopyThread")
        handlerThread.start()

        PixelCopy.request(arSceneView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { //api 28
                    val filename = generateFilename()
                    saveBitmapToGalley(bitmap, filename ?: return@request)
                } else {
                    saveBitmapToGallery(bitmap)
                }
                activity.runOnUiThread {
                    Toast.makeText(activity, "Successfully taken photo!", Toast.LENGTH_LONG).show()
                }
            } else {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Failed to take photo!", Toast.LENGTH_LONG).show()
                }
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }
}

