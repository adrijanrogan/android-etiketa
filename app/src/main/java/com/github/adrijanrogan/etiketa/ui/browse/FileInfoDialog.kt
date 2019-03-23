package com.github.adrijanrogan.etiketa.ui.browse

import android.os.Bundle
import android.text.format.DateUtils
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.github.adrijanrogan.etiketa.R
import java.io.File
import java.text.DateFormat
import java.util.*


class FileInfoDialog(val file: File) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_file_info, container, false)
        val name: TextView = view.findViewById(R.id.dialog_file_info_name_content)
        name.text = file.name
        val modified: TextView = view.findViewById(R.id.dialog_file_info_modified_content)
        val s = DateUtils.formatDateTime(activity!!, file.lastModified(), 0)
        modified.text = s
        val sizeContent: TextView = view.findViewById(R.id.dialog_file_info_size_content)
        val size: Double = file.length() / 1024.0
        sizeContent.text = String.format("%.2f kB", size)
        return view
    }
}