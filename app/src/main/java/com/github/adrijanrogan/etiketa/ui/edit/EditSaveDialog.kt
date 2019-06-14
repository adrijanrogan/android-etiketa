package com.github.adrijanrogan.etiketa.ui.edit

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


class EditSaveDialog(private val listener:EditSaveDialogListener) : DialogFragment() {

    interface EditSaveDialogListener {
        fun onDialogSaveButtonClick()
        fun onDialogDiscardButtonClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            setTitle(getString(R.string.unsaved_changes))
            setMessage(getString(R.string.unsaved_changes_details))
            setPositiveButton(getString(R.string.save)) { dialog, _ ->
                listener.onDialogSaveButtonClick()
                dialog.cancel()
            }
            setNeutralButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            setNegativeButton(getString(R.string.discard)) { dialog, _ ->
                listener.onDialogDiscardButtonClick()
                dialog.cancel()
            }
        }
        return builder.create()
    }
}