package com.hereliesaz.qard.widget

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.hereliesaz.qard.data.QrConfig
import java.io.File
import java.io.FileOutputStream

/**
 * Exports a [QrConfig] as a PNG image in the device gallery under Pictures/QaRd.
 *
 * On API 29+ this uses scoped storage (MediaStore RELATIVE_PATH) and needs no
 * permission. On API 26-28 the caller must hold WRITE_EXTERNAL_STORAGE.
 */
object QrImageExporter {

    private const val FOLDER = "QaRd"

    /**
     * @return the [Uri] of the saved image, or null if generation/saving failed.
     */
    fun saveToGallery(context: Context, config: QrConfig): Uri? {
        val bitmap = QrGenerator.generate(config) ?: run {
            Log.e("QrImageExporter", "QR generation returned null; nothing to save")
            return null
        }
        val fileName = "QaRd_${System.currentTimeMillis()}.png"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(context, bitmap, fileName)
            } else {
                saveLegacy(context, bitmap, fileName)
            }
        } catch (e: Exception) {
            Log.e("QrImageExporter", "Failed to save QR image", e)
            null
        }
    }

    private fun saveViaMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$FOLDER"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return null

        val success = try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: false
        } catch (e: Exception) {
            Log.e("QrImageExporter", "Failed to write bitmap to MediaStore", e)
            false
        }

        if (!success) {
            // Don't leave an orphaned pending entry in the gallery.
            resolver.delete(uri, null, null)
            return null
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val picturesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        val folder = File(picturesDir, FOLDER).apply { if (!exists()) mkdirs() }
        val file = File(folder, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Make the new file visible in the gallery.
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.DATA, file.absolutePath)
        }
        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }
}
