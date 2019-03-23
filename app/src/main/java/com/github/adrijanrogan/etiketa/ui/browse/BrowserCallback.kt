package com.github.adrijanrogan.etiketa.ui.browse

import java.io.File

interface BrowserCallback {

    fun onClickFile(file: File)

    fun showFileInfoDialog(file: File)
}