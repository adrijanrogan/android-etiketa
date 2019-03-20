package com.github.adrijanrogan.etiketa.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.adrijanrogan.etiketa.repository.FileRepository
import com.github.adrijanrogan.etiketa.util.FileComparator
import java.io.File

class BrowseViewModel : ViewModel() {

    companion object {
        const val SORTING_NAME_DESCENDING = 1
    }

    private val repository = FileRepository()

    fun getTree(): LiveData<List<File>> {
        return repository.getTree()
    }

    fun getFiles(showHidden: Boolean = false, sortOrder: Int = SORTING_NAME_DESCENDING):
            LiveData<List<File>> {
        val filesLiveData = repository.getFiles()
        return Transformations.map(filesLiveData) { files ->
            transformFiles(files, showHidden, sortOrder) }
    }

    private fun transformFiles(files: List<File>, showHidden: Boolean, sortOrder: Int): List<File> {
        val newList: MutableList<File> = ArrayList(files.size)
        if (!showHidden) {
            for (file in files) {
                if (!file.isHidden) newList.add(file)
            }
        }
        newList.sortWith(FileComparator())
        return newList
    }

    fun isRoot(): Boolean {
        return repository.checkIfRoot()
    }

    fun toParentFiles() {
        repository.toParentFiles()
    }

    fun toChildrenFiles(file: File) {
        repository.toChildrenFiles(file)
    }
}