package com.github.adrijanrogan.etiketa.jni

class FlacWriter(private val path: String) : Writer {

    init {
        System.loadLibrary("taglib")
    }

    override fun writeMetadata(metadata: Metadata): Int {
        val title = metadata.title
        val artist = metadata.artist
        val album = metadata.album
        val year = metadata.releaseYear
        val mimeType = metadata.imageMimeType
        val imageData = metadata.imageData
        return if (writeXiphComment(path, title, artist, album, year, mimeType, imageData) == 1) {
            Writer.METADATA_WRITE_SUCCESS
        } else
            Writer.METADATA_WRITE_FAILURE
    }

    private external fun writeXiphComment(filename: String, title: String?, artist: String?,
                                          album: String?, year: Int, mimeType: String?, jArray: ByteArray?): Int
}