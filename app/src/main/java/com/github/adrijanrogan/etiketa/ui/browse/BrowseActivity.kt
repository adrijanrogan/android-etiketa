package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.jni.FlacReader
import com.github.adrijanrogan.etiketa.jni.Metadata
import com.github.adrijanrogan.etiketa.jni.Mp3Reader
import com.github.adrijanrogan.etiketa.jni.Reader
import com.github.adrijanrogan.etiketa.ui.MetadataActivity
import java.io.File

class BrowseActivity : AppCompatActivity(), AdapterCallback {

    companion object {
        const val EXTENSION_MP3 = "mp3"
        const val EXTENSION_FLAC = "flac"
    }

    private lateinit  var viewModel: BrowseViewModel

    private var showHidden: Boolean = false // Ce false, skrijemo datoteke z zacetnico "."

    private var recyclerView: RecyclerView? = null
    private var noFiles: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        viewModel = ViewModelProviders.of(this).get(BrowseViewModel::class.java)

        showHidden = false
        recyclerView = findViewById(R.id.recycler)
        noFiles = findViewById(R.id.text_no_files)

        val layoutManager = LinearLayoutManager(this)
        recyclerView?.layoutManager = layoutManager
        updateUI()
    }


    override fun onClickFile(position: Int) {
        val currentChildren = viewModel.children
        if (position <= currentChildren.size) {
            val file = currentChildren[position]
            if (file.isDirectory) {
                viewModel.goDown(file)
                updateUI()
            } else {
                checkFile(file)
            }
        }
    }

    private fun updateUI() {
        viewModel.sortFiles()
        viewModel.removeHiddenFiles()
        val currentChildren = viewModel.children

        supportActionBar?.title = viewModel.getTitle(this)

        if (currentChildren.isEmpty()) {
            recyclerView?.visibility = View.GONE
            noFiles?.visibility = View.VISIBLE
        } else {
            noFiles?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            val adapter = BrowseAdapter(this, currentChildren, this)
            recyclerView?.swapAdapter(adapter, true)
        }
    }

    override fun onBackPressed() {
        if (viewModel.checkIfParentRoot()) {
            super.onBackPressed()
        } else {
            viewModel.goUp()
            updateUI()
        }
    }


    private fun checkFile(file: File) {
        // Morda je datoteka medtem bila izbrisana ali premaknjena.
        if (!file.exists()) {
            viewModel.goUp()
            updateUI()
            return
        }

        when (file.extension) {
            EXTENSION_MP3 -> {
                val reader: Reader = Mp3Reader(file.absolutePath)
                readMetadata(file, reader)
            }
            EXTENSION_FLAC -> {
                val reader: Reader = FlacReader(file.absolutePath)
                readMetadata(file, reader)
            }
            else -> Toast.makeText(this, "Format te datoteke ni podprt.",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun readMetadata(file: File, reader: Reader) {
        val metadata: Metadata
        when (reader.checkMetadata()) {
            Reader.NO_VALID_METADATA ->
                Toast.makeText(this, "Te datoteke ni bilo možno prebrati.",
                        Toast.LENGTH_LONG).show()
            Reader.METADATA_ID3v1 -> {
                metadata = reader.getMetadata()
                metadata.id3Version = 1
                runActivity(file, metadata)
            }
            Reader.METADATA_ID3v2 -> {
                metadata = reader.getMetadata()
                metadata.id3Version = 2
                runActivity(file, metadata)
            }
            Reader.METADATA_XIPH_COMMENT -> {
                metadata = reader.getMetadata()
                runActivity(file, metadata)
            }
        }
    }

    private fun runActivity(file: File, metadata: Metadata) {
        viewModel.setSelectedFile(file)
        metadata.writeImageToDisk(this)
        val bundle = metadata.toBundle()
        val intent = Intent(this, MetadataActivity::class.java)
        intent.putExtra("METADATA", bundle)
        intent.putExtra("FILE", file)
        startActivity(intent)
    }
}