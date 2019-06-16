package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
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
import com.github.adrijanrogan.etiketa.ui.edit.EditActivity
import com.github.adrijanrogan.etiketa.ui.settings.SettingsActivity
import com.github.adrijanrogan.etiketa.util.FileComparator
import java.io.File

class BrowseActivity : AppCompatActivity(), BrowserCallback, BrowserBarCallback {

    companion object {
        const val EXTENSION_MP3 = "mp3"
        const val EXTENSION_FLAC = "flac"
    }

    private lateinit  var viewModel: BrowseViewModel
    private lateinit var rootView: ViewGroup
    private lateinit var toolbar: Toolbar

    private lateinit var barRecyclerView: RecyclerView
    private lateinit var barRecyclerAdapter: BrowserBarAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: BrowseAdapter
    private lateinit var noFiles: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        rootView = findViewById(R.id.browser_root_view)
        toolbar = findViewById(R.id.browser_toolbar)
        toolbar.inflateMenu(R.menu.browser_toolbar_menu)
        toolbar.setOnMenuItemClickListener { onMenuItemClick(it) }

        viewModel = ViewModelProviders.of(this).get(BrowseViewModel::class.java)

        barRecyclerView = findViewById(R.id.browser_bar_recycler)
        barRecyclerView.also {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            barRecyclerAdapter = BrowserBarAdapter(this, this)
            it.adapter = barRecyclerAdapter
        }

        recyclerView = findViewById(R.id.browser_recycler)
        noFiles = findViewById(R.id.text_no_files)
        recyclerView.also {
            it.layoutManager = LinearLayoutManager(this)
            recyclerAdapter = BrowseAdapter(this, this)
            it.adapter = recyclerAdapter
        }

        observeLiveData()
    }

    private fun observeLiveData() {
        val sp = getSharedPreferences("browse_settings", Context.MODE_PRIVATE)
        val hidden = sp.getBoolean("BROWSE_SHOW_HIDDEN", false)
        val mode = sp.getInt("BROWSE_SORT_MODE", FileComparator.SORT_FOLDER_NAME)
        viewModel.getFiles().removeObservers(this)
        viewModel.getFiles(hidden, mode).observe(this, Observer { updateUI(it) })
        viewModel.getFiles().removeObservers(this)
        viewModel.getTree().observe(this, Observer { updateBarUI(it) })
    }


    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId
        val editor = getSharedPreferences("browse_settings", Context.MODE_PRIVATE).edit()
        when (id) {
            R.id.browser_menu_folders -> {
                menuItem.isChecked = !menuItem.isChecked
                editor.putBoolean("BROWSE_FOLDERS_EXTRA", menuItem.isChecked)
            }
            R.id.browser_menu_sort_hidden -> {
                menuItem.isChecked = !menuItem.isChecked
                editor.putBoolean("BROWSE_SHOW_HIDDEN", menuItem.isChecked)
            }
            R.id.browser_menu_sort_extension -> {
                menuItem.isChecked = !menuItem.isChecked
                editor.putBoolean("BROWSE_SORT_BY_EXTENSIONS", menuItem.isChecked)
            }
            R.id.browser_menu_sort_ascending -> {
                menuItem.isChecked = true
                editor.putInt("BROWSE_SORT_MODE", FileComparator.SORT_FOLDER_NAME)
                viewModel.getFiles().removeObservers(this)
            }
            R.id.browser_menu_sort_descending -> {
                menuItem.isChecked = true
                editor.putInt("BROWSE_SORT_MODE", FileComparator.SORT_FOLDER_NAME_REVERSED)
            }
            R.id.browser_menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        editor.apply()
        observeLiveData()
        return true
    }


    override fun onClickFile(file: File) {
        if (file.isDirectory) {
            viewModel.toChildrenFiles(file)
        } else {
            checkFile(file)
        }
    }

    override fun showFileInfoDialog(file: File) {
        val dialog = FileInfoDialog(file)
        dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)
        dialog.show(supportFragmentManager, "file_info_dialog")
    }

    override fun onClickTreeFile(file: File) {
        if (file.isDirectory) viewModel.toChildrenFiles(file)
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

    private fun updateBarUI(files: List<File>) {
        barRecyclerAdapter.submitList(files)
    }

    override fun onBackPressed() {
        if (viewModel.isRoot()) {
            super.onBackPressed()
        } else {
            viewModel.toParentFiles()
        }
    }


    private fun checkFile(file: File) {
        if (!file.exists()) {
            viewModel.toParentFiles()
            return
        }

        val reader: Reader
        when (file.extension) {
            EXTENSION_MP3 -> reader = Mp3Reader(file.absolutePath)
            EXTENSION_FLAC -> reader = FlacReader(file.absolutePath)
            else -> {
                Toast.makeText(this, getString(R.string.format_unsupported),
                        Toast.LENGTH_LONG).show()
                return
           }
        }

        readMetadata(file, reader)
    }

    private fun readMetadata(file: File, reader: Reader) {
        val check = reader.checkMetadata()
        if (check == Reader.NO_VALID_METADATA) {
            Toast.makeText(this, getString(R.string.unable_to_read),
                    Toast.LENGTH_LONG).show()
            return
        }

        val metadata = reader.getMetadata()
        // TODO: Save image async or use the bytes directly
        // (sometimes the picture takes a long time to save)
        metadata.writeImageToDisk(this)
        when (check) {
            Reader.METADATA_ID3v1 -> {
                metadata.id3Version = 1
            }
            Reader.METADATA_ID3v2 -> {
                metadata.id3Version = 2
            }
        }

        runActivity(file, metadata)
    }

    private fun runActivity(file: File, metadata: Metadata) {
        val bundle = metadata.toBundle()
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("METADATA", bundle)
        intent.putExtra("FILE", file)
        startActivity(intent)
    }
}