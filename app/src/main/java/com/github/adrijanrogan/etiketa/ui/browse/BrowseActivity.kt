package com.github.adrijanrogan.etiketa.ui.browse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.jni.FlacReader
import com.github.adrijanrogan.etiketa.jni.Metadata
import com.github.adrijanrogan.etiketa.jni.Mp3Reader
import com.github.adrijanrogan.etiketa.jni.Reader
import com.github.adrijanrogan.etiketa.ui.edit.EditActivity
import com.github.adrijanrogan.etiketa.ui.settings.SettingsActivity
import com.github.adrijanrogan.etiketa.ui.settings.SettingsFragment
import com.github.adrijanrogan.etiketa.util.FileComparator
import kotlinx.android.synthetic.main.activity_browser.*
import java.io.File

class BrowseActivity : AppCompatActivity(), BrowserCallback, BrowserBarCallback {

    companion object {
        const val EXTENSION_MP3 = "mp3"
        const val EXTENSION_FLAC = "flac"
        private const val PERMISSION_REQUEST = 9858
    }

    private lateinit  var viewModel: BrowseViewModel
    private lateinit var barRecyclerAdapter: BrowserBarAdapter
    private lateinit var recyclerAdapter: BrowseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            proceed()
        } else {
            permissionFlow()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SettingsFragment.invalidateSort) {
            observeLiveData()
            syncMenu(browser_toolbar.menu)
        }
        SettingsFragment.invalidateSort = false
    }

    private fun permissionFlow() {
        androidx.transition.TransitionManager.beginDelayedTransition(browser_root_view, Fade())
        text_no_files.visibility = View.INVISIBLE
        browser_recycler.visibility = View.INVISIBLE
        browser_bar_recycler.visibility = View.INVISIBLE
        permission_request.visibility = View.VISIBLE
        permission_button.setOnClickListener { requestPermission() }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceed()
            } else {
                Toast.makeText(this,
                        "Prosimo sprejmite zahtevo za dostop do pomnilnika",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun proceed() {
        androidx.transition.TransitionManager.beginDelayedTransition(browser_root_view, Fade())
        permission_request.visibility = View.GONE
        text_no_files.visibility = View.INVISIBLE
        browser_recycler.visibility = View.VISIBLE
        browser_bar_recycler.visibility = View.VISIBLE

        viewModel = ViewModelProviders.of(this).get(BrowseViewModel::class.java)

        browser_toolbar.inflateMenu(R.menu.browser_toolbar_menu)
        syncMenu(browser_toolbar.menu)
        browser_toolbar.setOnMenuItemClickListener { onMenuItemClick(it) }

        browser_bar_recycler.also {
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            barRecyclerAdapter = BrowserBarAdapter(this, this)
            it.adapter = barRecyclerAdapter
        }

        browser_recycler.also {
            it.layoutManager = LinearLayoutManager(this)
            recyclerAdapter = BrowseAdapter(this, this)
            it.adapter = recyclerAdapter
        }

        observeLiveData()
    }

    private fun syncMenu(menu: Menu?) {
        if (menu == null) return
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        when (SettingsFragment.getSortBy(sp)) {
            FileComparator.SORT_MODE_LAST_MODIFIED ->
                menu.findItem(R.id.browser_menu_sort_by_last_modified).isChecked = true
            else -> menu.findItem(R.id.browser_menu_sort_by_name).isChecked = true
        }

        val hidden = SettingsFragment.getSortShowHidden(sp)
        menu.findItem(R.id.browser_menu_sort_hidden).isChecked = hidden

        val group = SettingsFragment.getSortGroup(sp)
        val groupBox = menu.findItem(R.id.browser_menu_folders)
        when (group) {
            FileComparator.SORT_MODE_GROUP -> groupBox.isChecked = true
            else -> groupBox.isChecked = false
        }

        when (SettingsFragment.getSortOrder(sp)) {
            FileComparator.SORT_MODE_NORMAL ->
                menu.findItem(R.id.browser_menu_sort_ascending).isChecked = true
            else -> menu.findItem(R.id.browser_menu_sort_descending).isChecked = true
        }
    }


    private fun observeLiveData() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val hidden = SettingsFragment.getSortShowHidden(sp)
        val sortBy = SettingsFragment.getSortBy(sp)
        val group = SettingsFragment.getSortGroup(sp)
        val order = SettingsFragment.getSortOrder(sp)
        val sortMode = (sortBy or group or order)
        viewModel.getFiles().removeObservers(this)
        viewModel.getFiles(hidden, sortMode).observe(this, Observer { updateUI(it) })
        viewModel.getFiles().removeObservers(this)
        viewModel.getTree().observe(this, Observer { updateBarUI(it) })
    }


    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        when (id) {
            R.id.browser_menu_sort_by_name -> {
                menuItem.isChecked = true
                editor.putString(SettingsFragment.PREFERENCE_SORTING_BY,
                        SettingsFragment.PREFERENCE_SORTING_BY_NAME)
            }
            R.id.browser_menu_sort_by_last_modified -> {
                menuItem.isChecked = true
                editor.putString(SettingsFragment.PREFERENCE_SORTING_BY,
                        SettingsFragment.PREFERENCE_SORTING_BY_LAST_MODIFIED)
            }
            R.id.browser_menu_folders -> {
                val group = !menuItem.isChecked
                menuItem.isChecked = group
                editor.putBoolean(SettingsFragment.PREFERENCE_SORTING_GROUP, group)
            }
            R.id.browser_menu_sort_hidden -> {
                val showHidden = !menuItem.isChecked
                menuItem.isChecked = showHidden
                editor.putBoolean(SettingsFragment.PREFERENCE_SORTING_SHOW_HIDDEN, showHidden)
            }
            R.id.browser_menu_sort_ascending -> {
                menuItem.isChecked = true
                editor.putString(SettingsFragment.PREFERENCE_SORTING_ORDER,
                        SettingsFragment.PREFERENCE_SORTING_ORDER_NORMAL)
            }
            R.id.browser_menu_sort_descending -> {
                menuItem.isChecked = true
                editor.putString(SettingsFragment.PREFERENCE_SORTING_ORDER,
                        SettingsFragment.PREFERENCE_SORTING_ORDER_REVERSED)
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
            showLoadingView(true)
            viewModel.toChildrenFiles(file)
        } else {
            checkFile(file)
        }
    }

    private fun showLoadingView(show: Boolean) {
        progress_parent.visibility = if (show) View.VISIBLE else View.GONE
        browser_recycler.visibility = if (show) View.GONE else View.VISIBLE
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
        showLoadingView(false)
        if (files.isEmpty()) {
            TransitionManager.beginDelayedTransition(browser_root_view, Fade())
            browser_recycler.visibility = View.GONE
            text_no_files.visibility = View.VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(browser_root_view, Fade())
            text_no_files.visibility = View.GONE
            browser_recycler.visibility = View.VISIBLE
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