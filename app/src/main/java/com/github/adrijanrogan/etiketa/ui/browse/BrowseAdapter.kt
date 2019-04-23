package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.adrijanrogan.etiketa.R
import java.io.File

class BrowseAdapter(private val context: Context, private val callback: BrowserCallback) :
        ListAdapter<File, BrowseAdapter.FileViewHolder>(DIFF_CALLBACK) {


    inner class FileViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
        var fileIcon: ImageView = root.findViewById(R.id.holder_icon)
        var fileName: TextView = root.findViewById(R.id.holder_file_name)
        var subFiles: TextView = root.findViewById(R.id.holder_sub_files)
        var options: ImageView = root.findViewById(R.id.holder_options)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.holder_file, parent, false)
        return FileViewHolder(root)
    }


    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        val fileName = file.name

        holder.root.setOnClickListener { callback.onClickFile(getItem(position)) }
        holder.options.setOnClickListener { showPopupMenu(it, getItem(position)) }

        if (file.isHidden) {
            holder.fileName.alpha = 0.7F
            holder.subFiles.alpha = 0.7F
            holder.fileIcon.imageAlpha = 127
        } else {
            holder.fileName.alpha = 1.0F
            holder.subFiles.alpha = 1.0F
            holder.fileIcon.imageAlpha = 255
        }

        holder.fileName.text = fileName
        if (file.isDirectory) {
            holder.subFiles.visibility = View.VISIBLE
            val count = file.listFiles().size
            if (count == 0) holder.subFiles.text = context.getString(R.string.numberOfFilesEmpty)
            else holder.subFiles.text = context.resources.getQuantityString(
                    R.plurals.numberOfFiles, count, count)
        } else {
            holder.subFiles.visibility = View.GONE
        }

        // TODO: Move this to something like an utility class


        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_outline_folder)
        } else if (fileName.endsWith("mp3") || fileName.endsWith("flac")) {
            holder.fileIcon.setImageResource(R.drawable.ic_outline_music_note)
        } else if (fileName.endsWith("png") || fileName.endsWith("jpg") ||
                fileName.endsWith("jpeg")) {
            holder.fileIcon.setImageResource(R.drawable.ic_outline_photo)
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_outline_file)
        }

    }

    private fun showPopupMenu(view: View, file: File) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.browser_file_menu, popup.menu)
        popup.setOnMenuItemClickListener { onPopupMenuItemClicked(it, file) }
        popup.show()
    }


    private fun onPopupMenuItemClicked(menuItem: MenuItem, file: File): Boolean {
        when (menuItem.itemId) {
            R.id.browser_menu_show_info -> {
                callback.showFileInfoDialog(file)
            }
            R.id.browser_menu_as_album ->
                Toast.makeText(context, "TODO: Treat as album", Toast.LENGTH_LONG).show()
        }
        return true
    }


    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<File> = object:DiffUtil.ItemCallback<File>() {
            // Must compare paths, otherwise folders with same names cause trouble
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.absolutePath == newItem.absolutePath
            }

            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }
}