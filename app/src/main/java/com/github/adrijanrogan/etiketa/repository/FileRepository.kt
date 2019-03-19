package com.github.adrijanrogan.etiketa.repository

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class FileRepository {

    private val root = File(Environment.getExternalStorageDirectory().path + "/")
    private var currentFile = root

    private val fileLiveData = MutableLiveData<List<File>>()

    init {
        fileLiveData.postValue(root.listFiles().asList())
    }


    fun getFiles(): LiveData<List<File>> {
        return fileLiveData
    }

    fun toParentFiles() {
        if (!checkIfRoot()) {
            currentFile = currentFile.parentFile
            fileLiveData.postValue(currentFile.listFiles().asList())
        }
    }

    fun toChildrenFiles(file: File) {
        currentFile = file
        fileLiveData.postValue(currentFile.listFiles().asList())
    }


    fun checkIfRoot(): Boolean {
        return root.absolutePath == currentFile.absolutePath
    }



}