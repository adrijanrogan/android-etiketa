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
import kotlinx.android.synthetic.main.activity_edit.*
import java.io.File

class EditActivity : AppCompatActivity() {

    private lateinit var viewModel: EditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val metadata = intent.getBundleExtra("METADATA")
        val file = intent.getSerializableExtra("FILE") as File
        viewModel = ViewModelProviders.of(this).get(EditViewModel::class.java)
        viewModel.setup(file, metadata)

        edit_toolbar.setNavigationOnClickListener { onToolbarNavigationClick() }
        button_save.setOnClickListener { onSaveButtonClick() }

        setImage(file, metadata)
        text_title.setText(viewModel.getOriginalTitle())
        text_artist.setText(viewModel.getOriginalArtist())
        text_album.setText(viewModel.getOriginalAlbum())
        text_year.setText(viewModel.getOriginalYear())
    }

    private fun setImage(file: File, metadata: Bundle) {
        if (viewModel.getImagePath().isNotBlank()) {
            image.visibility = View.VISIBLE
            text_image.visibility = View.GONE
            val albumArt = BitmapFactory.decodeFile(viewModel.getImagePath())
            image.setImageBitmap(albumArt)
        } else if (file.name.endsWith(".mp3") && metadata.getInt("ID3") == 1) {
            text_image.setText(R.string.id3_older_format)
            image.visibility = View.GONE
            text_image.visibility = View.VISIBLE
        } else {
            text_image.setText(R.string.picture_not_found)
            image.visibility = View.GONE
            text_image.visibility = View.VISIBLE
        }
    }

    private fun onToolbarNavigationClick() {
        if (hasMetadataChanged()) {
            // TODO: Show dialog to warn user about saving
            closeActivity()
        } else {
            closeActivity()
        }
    }

    private fun onSaveButtonClick() {
        if (hasMetadataChanged()) writeChanges() else {
            postResult(2)
            closeActivity()
        }
    }

    private fun hasMetadataChanged(): Boolean {
        val title = text_title.text.toString()
        val artist = text_artist.text.toString()
        val album = text_album.text.toString()
        val year = text_year.text.toString().toIntOrNull() ?: 0
        val metadata = Metadata(title, artist, album, year, null, null)
        viewModel.setNewMetadata(metadata)
        return viewModel.metadataChanged()
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
        closeActivity()
    }

    private fun closeActivity() {
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