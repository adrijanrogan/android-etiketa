package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.util.FileComparator
import java.io.File
import java.util.*

class BrowseViewModel : ViewModel() {

    // Vedno hranimo referenco parent, da se lahko enostavneje vrnemo v visjo hierarhijo.
    // Ce sta parent in root enaka, smo ze najvisje.
    // Uporabnik se v visjo hierarhijo vraca s tipko nazaj, v primeru, da smo ze najvisje,
    // pa se aplikacija (kot je obicajno za tipko nazaj) zapre.
    private lateinit var root: File
    private lateinit var parent: File
    lateinit var children: Array<File>
        private set

    private var selectedFile: File? = null

    init {
        setup()
    }

    private fun setup() {
        root = File(Environment.getExternalStorageDirectory().path + "/")
        parent = root
        children = parent.listFiles()
    }

    internal fun goUp() {
        parent = parent.parentFile
        children = parent.listFiles()
    }

    internal fun goDown(file: File) {
        parent = file
        children = parent.listFiles()
    }


    internal fun checkIfParentRoot(): Boolean {
        return parent.absolutePath == root.absolutePath
    }

    internal fun getTitle(context: Context): String {
        return if (checkIfParentRoot()) {
            context.getString(R.string.internal_storage)
        } else {
            parent.name
        }
    }


    internal fun sortFiles() {
        Arrays.sort(children, FileComparator())
    }

    internal fun removeHiddenFiles() {
        val fileList = ArrayList(Arrays.asList(*children))
        for (i in fileList.indices.reversed()) {
            val file = fileList[i]
            if (file.name.startsWith(".")) {
                fileList.removeAt(i)
            }
        }
        children = fileList.toTypedArray()
    }

    internal fun setSelectedFile(file: File) {
        selectedFile = file
    }
}