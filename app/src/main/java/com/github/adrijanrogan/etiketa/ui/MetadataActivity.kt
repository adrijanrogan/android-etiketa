package com.github.adrijanrogan.etiketa.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.jni.FlacWriter
import com.github.adrijanrogan.etiketa.jni.Metadata
import com.github.adrijanrogan.etiketa.jni.Mp3Writer
import com.github.adrijanrogan.etiketa.jni.Writer
import java.io.ByteArrayOutputStream
import java.io.File

class MetadataActivity : AppCompatActivity() {

    private lateinit var file: File
    private val imageChanged: Boolean = false

    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var artistEdit: EditText
    private lateinit var albumEdit: EditText
    private lateinit var yearEdit: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_metadata)
        // Iz intenta dobimo Bundle, ki hrani metapodatke, in objekt File.
        val metadata = intent.getBundleExtra("METADATA")
        file = intent.getSerializableExtra("FILE") as File

        val imagePath = metadata.getString("IMAGE_PATH")
        val title = metadata.getString("TITLE")
        val artist = metadata.getString("ARTIST")
        val album = metadata.getString("ALBUM")
        val year = metadata.getInt("YEAR")

        // Poiscemo nase komponente uporabniskega vmesnika.
        imageView = findViewById(R.id.image)
        titleEdit = findViewById(R.id.text_title)
        artistEdit = findViewById(R.id.text_artist)
        albumEdit = findViewById(R.id.text_album)
        yearEdit = findViewById(R.id.text_year)
        buttonSave = findViewById(R.id.button_save)

        // Kot naslov Activity damo kar ime datoteke.
        supportActionBar?.title = file.name

        // Vstavimo sliko, ce obstaja. Sicer skrijemo sliko in zapisemo razlog.
        if (imagePath != null) {
            imageView.visibility = View.VISIBLE
            val albumArt = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(albumArt)
        } else if (file.name.endsWith(".mp3") && metadata.getInt("ID3") == 1) {
            val textImage = findViewById<TextView>(R.id.text_image)
            textImage.setText(R.string.id3_older_format)
            imageView.visibility = View.GONE
            textImage.visibility = View.VISIBLE
        } else {
            val textImage = findViewById<TextView>(R.id.text_image)
            textImage.setText(R.string.picture_not_found)
            imageView.visibility = View.GONE
            textImage.visibility = View.VISIBLE
        }


        titleEdit.setText(title)
        artistEdit.setText(artist)
        albumEdit.setText(album)
        yearEdit.setText(year.toString())

        buttonSave.setOnClickListener {
            if (compareData(metadata)) {
                val newMetadata = makeMetadata(metadata)
                val writer: Writer
                when {
                    file.name.endsWith(".mp3") -> {
                        writer = Mp3Writer(file.absolutePath)
                        val s = writer.writeMetadata(newMetadata)
                        postResult(s)
                    }
                    file.name.endsWith(".flac") -> {
                        writer = FlacWriter(file.absolutePath)
                        val s = writer.writeMetadata(newMetadata)
                        postResult(s)
                    }
                    else -> Toast.makeText(this, "Interna napaka.", Toast.LENGTH_LONG).show()
                }
            } else {
                postResult(2)
            }
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    private fun postResult(s: Int) {
        when (s) {
            0 -> Toast.makeText(this, getString(R.string.error_occured),
                    Toast.LENGTH_LONG).show()
            1 -> Toast.makeText(this, getString(R.string.successfully_saved),
                    Toast.LENGTH_LONG).show()
            2 -> Toast.makeText(this, getString(R.string.remained_unchanged),
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun compareData(metadata: Bundle): Boolean {
        val title = metadata.getString("TITLE")
        val artist = metadata.getString("ARTIST")
        val album = metadata.getString("ALBUM")
        val year = metadata.getInt("YEAR")

        var newYear = 0
        if (yearEdit.text.toString() != "") {
            newYear = Integer.valueOf(yearEdit.text.toString())
        }
        return imageChanged || titleEdit.text.toString() != title ||
                artistEdit.text.toString() != artist ||
                albumEdit.text.toString() != album ||
                newYear != year
    }

    private fun makeMetadata(oldMetadata: Bundle): Metadata {

        val oldTitle = oldMetadata.getString("TITLE")
        val oldArtist = oldMetadata.getString("ARTIST")
        val oldAbum = oldMetadata.getString("ALBUM")
        val oldYear = oldMetadata.getInt("YEAR")

        var title: String? = oldTitle
        var artist: String? = oldArtist
        var album: String? = oldAbum
        var mimeType: String? = null
        var year = oldYear
        var imageData: ByteArray? = null

        if (titleEdit.text.toString() != oldTitle) {
            title = titleEdit.text.toString()
        }

        if (artistEdit.text.toString() != oldArtist) {
            artist = artistEdit.text.toString()
        }

        if (albumEdit.text.toString() != oldAbum) {
            album = albumEdit.text.toString()
        }

        if (yearEdit.text.toString().toIntOrNull() != null) {
            year = yearEdit.text.toString().toIntOrNull()!!
        }

        if (imageChanged) {
            val image = (imageView.drawable as BitmapDrawable).bitmap
            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            imageData = outputStream.toByteArray()
            mimeType = "image/jpeg"
        }

        return Metadata(title, artist, album, year, mimeType, imageData)
    }
}