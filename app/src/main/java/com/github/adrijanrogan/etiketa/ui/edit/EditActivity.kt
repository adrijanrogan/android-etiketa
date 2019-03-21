package com.github.adrijanrogan.etiketa.ui.edit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.jni.FlacWriter
import com.github.adrijanrogan.etiketa.jni.Metadata
import com.github.adrijanrogan.etiketa.jni.Mp3Writer
import com.github.adrijanrogan.etiketa.jni.Writer
import java.io.File

class EditActivity : AppCompatActivity() {

    private lateinit var viewModel: EditViewModel

    private lateinit var imageView: ImageView
    private lateinit var titleEdit: EditText
    private lateinit var artistEdit: EditText
    private lateinit var albumEdit: EditText
    private lateinit var yearEdit: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        imageView = findViewById(R.id.image)
        titleEdit = findViewById(R.id.text_title)
        artistEdit = findViewById(R.id.text_artist)
        albumEdit = findViewById(R.id.text_album)
        yearEdit = findViewById(R.id.text_year)
        buttonSave = findViewById(R.id.button_save)

        val metadata = intent.getBundleExtra("METADATA")
        val file = intent.getSerializableExtra("FILE") as File

        viewModel = ViewModelProviders.of(this).get(EditViewModel::class.java)
        viewModel.setup(file, metadata)

        if (viewModel.getImagePath().isNotBlank()) {
            imageView.visibility = View.VISIBLE
            val albumArt = BitmapFactory.decodeFile(viewModel.getImagePath())
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


        titleEdit.setText(viewModel.getOriginalTitle())
        artistEdit.setText(viewModel.getOriginalArtist())
        albumEdit.setText(viewModel.getOriginalAlbum())
        yearEdit.setText(viewModel.getOriginalYear())

        buttonSave.setOnClickListener { compareMetadata() }
    }


    private fun compareMetadata() {
        val title = titleEdit.text.toString()
        val artist = artistEdit.text.toString()
        val album = albumEdit.text.toString()
        val year = yearEdit.text.toString().toIntOrNull() ?: 0
        val metadata = Metadata(title, artist, album, year, null, null)
        viewModel.setNewMetadata(metadata)
        if (viewModel.metadataChanged()) writeChanges()
        else postResult(2)
    }

    private fun writeChanges() {
        val file = viewModel.getFile()
        val writer: Writer
        when {
            file.name.endsWith(".mp3") -> writer = Mp3Writer(file.absolutePath)
            file.name.endsWith(".flac") -> writer = FlacWriter(file.absolutePath)
            else -> {
                Toast.makeText(this, "Interna napaka.", Toast.LENGTH_LONG).show()
                return
            }
        }
        val s = writer.writeMetadata(viewModel.getNewMetadata())
        when (s) {
            Writer.METADATA_WRITE_FAILURE -> postResult(0)
            Writer.METADATA_WRITE_SUCCESS -> postResult(1)
        }

        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
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
}