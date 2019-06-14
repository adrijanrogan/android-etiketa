package com.github.adrijanrogan.etiketa.ui.browse

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.github.adrijanrogan.etiketa.R
import com.github.adrijanrogan.etiketa.util.getFileSize
import java.io.File


class FileInfoDialog() : DialogFragment() {

    private var file: File? = null

    constructor(file: File) : this() {
        this.file = file
    }

    @SuppressLint("InflateParams") // null in docs
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null) file = File(savedInstanceState.getString("FILE"))
        return activity!!.let {
            val inflater = it.layoutInflater
            val builder = AlertDialog.Builder(it)
            val view = inflater.inflate(R.layout.dialog_file_info, null)
            setInformation(view)
            builder.setView(view)
                    .setTitle("More information")
                    .setPositiveButton("Close") { dialog, _ -> dialog.cancel() }
                    .create()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("FILE", file?.absolutePath)
    }

    private fun setInformation(view: View) {
        val nameView: TextView = view.findViewById(R.id.dialog_file_info_name_content)
        nameView.text = file?.name ?: "<unknown>"
        val pathView: TextView = view.findViewById(R.id.dialog_file_info_path_content)
        pathView.text = file?.absolutePath ?: "<unknown>"

        val modifiedView: TextView = view.findViewById(R.id.dialog_file_info_modified_content)
        file?.lastModified()?.let {
            modifiedView.text = DateUtils.formatDateTime(activity!!, it, 0)
        }

        val sizeView: TextView = view.findViewById(R.id.dialog_file_info_size_content)

        val (file, unit) = getFileSize(file?.length() ?: 0)
        sizeView.text = String.format("%.2f %s", file, unit)
    }
}