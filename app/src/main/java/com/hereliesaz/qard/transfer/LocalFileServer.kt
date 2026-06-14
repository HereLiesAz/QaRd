package com.hereliesaz.qard.transfer

import android.content.Context
import android.net.Uri
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.util.UUID

/**
 * A tiny one-file HTTP server for same-Wi-Fi hand-off. The sender shows a QR with this
 * server's URL; a receiver on the same network opens it with their stock camera and the
 * browser downloads the file directly — no app needed on the receiving end.
 *
 * Security model for a browser receiver (which can't run any client-side decryption): the
 * file is served as-is behind an unguessable [token] path, so only someone who scanned the
 * QR can fetch it, and only while the server is running. Bind on port 0 for an ephemeral
 * port; read [listeningPort] after [start].
 */
class LocalFileServer(
    private val appContext: Context,
    private val fileUri: Uri,
    private val fileName: String,
    private val mimeType: String,
    private val fileSize: Long,
    port: Int = 0,
) : NanoHTTPD(port) {

    /** Unguessable path segment included in the QR'd URL. */
    val token: String = UUID.randomUUID().toString().replace("-", "")

    private val downloadPath = "/d/$token"

    override fun serve(session: IHTTPSession): Response {
        if (session.method != Method.GET || session.uri != downloadPath) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
        val input = try {
            appContext.contentResolver.openInputStream(fileUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open file for serving", e)
            null
        } ?: return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Cannot read file"
        )

        val response = if (fileSize >= 0) {
            newFixedLengthResponse(Response.Status.OK, mimeType, input, fileSize)
        } else {
            newChunkedResponse(Response.Status.OK, mimeType, input)
        }
        response.addHeader("Content-Disposition", "attachment; filename=\"${sanitize(fileName)}\"")
        return response
    }

    /** The URL a receiver opens, e.g. `http://192.168.1.5:43215/d/<token>`. */
    fun url(host: String): String = "http://$host:$listeningPort$downloadPath"

    // Strip characters that could break out of the Content-Disposition header.
    private fun sanitize(name: String): String =
        name.replace("\"", "").replace("\r", "").replace("\n", "")

    companion object {
        private const val TAG = "LocalFileServer"
    }
}
