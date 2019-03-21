package com.github.adrijanrogan.etiketa.ui.edit

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.github.adrijanrogan.etiketa.jni.Metadata
import java.io.File

class EditViewModel : ViewModel() {

    private lateinit var file: File
    private var imagePath: String = ""

    private lateinit var oldMetadata: Metadata
    private lateinit var newMetadata: Metadata

    fun setup(file: File, metadataBundle: Bundle) {
        this.file = file
        oldMetadata = convertBundle(metadataBundle)
        newMetadata = convertBundle(metadataBundle)
    }

    private fun convertBundle(metadataBundle: Bundle): Metadata {
        imagePath = metadataBundle.getString("IMAGE_PATH") ?: ""
        val title = metadataBundle.getString("TITLE")
        val artist = metadataBundle.getString("ARTIST")
        val album = metadataBundle.getString("ALBUM")
        val year = metadataBundle.getInt("YEAR")
        return Metadata(title, artist, album, year, null, null)
    }

    fun getFile(): File { return file }
    fun getImagePath(): String { return imagePath }

    fun getOriginalTitle(): String { return oldMetadata.title ?: "" }
    fun getOriginalAlbum(): String { return oldMetadata.album ?: "" }
    fun getOriginalArtist(): String { return oldMetadata.artist ?: "" }
    fun getOriginalYear(): String { return oldMetadata.releaseYear.toString() }

    fun setNewMetadata(m: Metadata) { newMetadata = m }
    fun getNewMetadata(): Metadata { return newMetadata }

    fun metadataChanged(): Boolean {
        return !oldMetadata.compareTo(newMetadata)
    }

}