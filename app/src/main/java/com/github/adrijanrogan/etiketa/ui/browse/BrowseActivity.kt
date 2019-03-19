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

    private lateinit  var viewModel: BrowseViewModel

    private var showHidden: Boolean = false // Ce false, skrijemo datoteke z zacetnico "."

    private var recyclerView: RecyclerView? = null
    private var noFiles: TextView? = null

    // Vstopna tocka v BrowseActivity.
    // Dolocimo postavitev, ki jo zelimo pokazati uporabniku (activity_browser).
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

    // Klik na mapo -> pokazemo novo hierarhijo,
    // sicer -> ce je datoteka podprta, omogocimo spreminjanje metapodatkov te datoteke.
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

        val path = file.absolutePath
        val metadata: Metadata

        // Preveri, ali je datoteka mp3 ali flac, saj uporabljata razlicen nacin zapisovanja
        // metapodatkov. Mozno je tudi, da je datoteka poskodovana ali pa sploh ni tega formata.
        // Za vse druge formate datotek uporabniku javimo, da format ni podprt.

        // Vrne vse od zadnje pike naprej.
        // Primer: path = "home/adrijan/foo.mp3" --> format = ".mp3"
        val format = path.substring(path.lastIndexOf("."))
        val reader: Reader
        when (format) {
            ".mp3" -> {
                reader = Mp3Reader(path)
                when (reader.checkMetadata()) {
                    Reader.NO_VALID_METADATA ->
                        Toast.makeText(this, "Te datoteke ni bilo možno prebrati.",
                            Toast.LENGTH_LONG).show()
                    // Za ID3 verzija 1.
                    Reader.METADATA_ID3v1 -> {
                        metadata = reader.getMetadata()
                        metadata.id3Version = 1
                        runActivity(file, metadata)
                    }
                    // Za ID3 verzija 2.
                    Reader.METADATA_ID3v2 -> {
                        metadata = reader.getMetadata()
                        metadata.id3Version = 2
                        runActivity(file, metadata)
                    }
                }
            }
            ".flac" -> {
                reader = FlacReader(path)
                if (reader.checkMetadata() == Reader.NO_VALID_METADATA) {
                    Toast.makeText(this, "Te datoteke ni bilo možno prebrati.",
                            Toast.LENGTH_LONG).show()
                } else {
                    metadata = reader.getMetadata()
                    runActivity(file, metadata)
                }
            }
            else -> Toast.makeText(this, "Format te datoteke ni podprt.",
                    Toast.LENGTH_LONG).show()
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