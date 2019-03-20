package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
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

    private var showHidden: Boolean = false

    private lateinit  var viewModel: BrowseViewModel
    private lateinit var rootView: ViewGroup
    private lateinit var toolbar: Toolbar

    private lateinit var treeRecyclerView: RecyclerView
    private lateinit var treeRecyclerAdapter: BrowseTreeAdapter


    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: BrowseAdapter
    private lateinit var noFiles: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        viewModel = ViewModelProviders.of(this).get(BrowseViewModel::class.java)

        rootView = findViewById(R.id.browser_root_view)

        toolbar = findViewById(R.id.browser_toolbar)
        toolbar.inflateMenu(R.menu.browser_toolbar_menu)

        treeRecyclerView = findViewById(R.id.browser_tree_recycler)
        treeRecyclerView.also {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            treeRecyclerAdapter = BrowseTreeAdapter(this)
            it.adapter = treeRecyclerAdapter
        }

        recyclerView = findViewById(R.id.browser_recycler)
        noFiles = findViewById(R.id.text_no_files)
        recyclerView.also {
            it.layoutManager = LinearLayoutManager(this)
            recyclerAdapter = BrowseAdapter(this, this)
            it.adapter = recyclerAdapter
        }

        viewModel.getFiles().observe(this, Observer { updateUI(it) })
        viewModel.getTree().observe(this, Observer { treeRecyclerAdapter.submitList(it) })
    }


    override fun onClickFile(file: File) {
        if (file.isDirectory) {
            viewModel.toChildrenFiles(file)
        } else {
            checkFile(file)
        }
    }

    private fun updateUI(files: List<File>) {
        if (files.isEmpty()) {
            androidx.transition.TransitionManager.beginDelayedTransition(rootView, Fade())
            recyclerView.visibility = View.GONE
            noFiles.visibility = View.VISIBLE
        } else {
            androidx.transition.TransitionManager.beginDelayedTransition(rootView, Fade())
            noFiles.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerAdapter.submitList(files)
        }
    }

    override fun onBackPressed() {
        if (viewModel.isRoot()) {
            super.onBackPressed()
        } else {
            viewModel.toParentFiles()
        }
    }


    private fun checkFile(file: File) {
        // Morda je datoteka medtem bila izbrisana ali premaknjena.
        if (!file.exists()) {
            viewModel.toParentFiles()
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
        metadata.writeImageToDisk(this)
        val bundle = metadata.toBundle()
        val intent = Intent(this, MetadataActivity::class.java)
        intent.putExtra("METADATA", bundle)
        intent.putExtra("FILE", file)
        startActivity(intent)
    }
}