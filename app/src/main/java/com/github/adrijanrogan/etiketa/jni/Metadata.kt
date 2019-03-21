package com.github.adrijanrogan.etiketa.jni

import android.content.Context
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// TODO: Enforce non null values
class Metadata(val title: String?, val artist: String?, val album: String?, val releaseYear: Int,
               val imageMimeType: String?, val imageData: ByteArray?) {

    var imagePath: String? = null
        private set

    var id3Version = 0

    fun compareTo(m: Metadata): Boolean {
        return title == m.title && artist == m.artist && album == m.album &&
                releaseYear == m.releaseYear
    }

    fun writeImageToDisk(context: Context) {
        if (imageData != null) {
            val folder = context.getDir("pictures", Context.MODE_PRIVATE)
            val image = File(folder, title + artist)
            try {
                val stream = FileOutputStream(image)
                stream.write(imageData)
                imagePath = image.absolutePath
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString("TITLE", title)
        bundle.putString("ARTIST", artist)
        bundle.putString("ALBUM", album)
        bundle.putInt("YEAR", releaseYear)
        bundle.putString("IMAGE_MIME", imageMimeType)
        bundle.putString("IMAGE_PATH", imagePath)
        bundle.putInt("ID3", id3Version)
        return bundle
    }
}