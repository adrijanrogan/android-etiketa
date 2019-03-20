package com.github.adrijanrogan.etiketa.repository

import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class FileRepository {

    private val root = File(Environment.getExternalStorageDirectory().path + "/")
    private var currentFile = root

    private val fileLiveData = MutableLiveData<List<File>>()
    private val treeLiveData = MutableLiveData<List<File>>()


    init {
        fileLiveData.postValue(root.listFiles().asList())
        treeLiveData.postValue(listOf(root))
    }

    fun getTree(): LiveData<List<File>> {
        return treeLiveData
    }

    fun getFiles(): LiveData<List<File>> {
        return fileLiveData
    }

    fun toParentFiles() {
        if (!checkIfRoot()) {
            currentFile = currentFile.parentFile
            fileLiveData.postValue(currentFile.listFiles().asList())
            // val tree = treeLiveData.value?.toMutableList()?.dropLast(1)
            treeLiveData.postValue(buildFileTree())
        }
    }

    fun toChildrenFiles(file: File) {
        currentFile = file
        fileLiveData.postValue(currentFile.listFiles().asList())
        val tree = buildFileTree()
        Log.d("FileRepository", "Posting $tree")
        treeLiveData.postValue(tree)
    }


    private fun buildFileTree(): List<File> {
        val tree: MutableList<File> = ArrayList()
        var tmp = currentFile
        tree.add(tmp)
        while (tmp != root && tmp.parentFile != null) {
            tmp = tmp.parentFile
            tree.add(tmp)
        }
        return tree.reversed()
    }


    fun checkIfRoot(): Boolean {
        return root.absolutePath == currentFile.absolutePath
    }



}