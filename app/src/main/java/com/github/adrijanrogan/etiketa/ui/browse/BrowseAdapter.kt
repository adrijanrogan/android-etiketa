package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.adrijanrogan.etiketa.R
import java.io.File

class BrowseAdapter(
        private val context: Context,
        private val files: Array<File>,
        private val callback: AdapterCallback)
    : RecyclerView.Adapter<BrowseAdapter.FileViewHolder>() {
    
    inner class FileViewHolder(root: View) : RecyclerView.ViewHolder(root), View.OnClickListener {

        var fileIcon: ImageView
        var fileName: TextView
        var subFiles: TextView

        init {
            root.setOnClickListener(this)
            this.fileIcon = root.findViewById(R.id.holder_icon)
            this.fileName = root.findViewById(R.id.holder_file_name)
            this.subFiles = root.findViewById(R.id.holder_sub_files)
        }

        override fun onClick(v: View) {
            callback.onClickFile(layoutPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.holder_file, parent, false)
        return FileViewHolder(root)
    }

    // Tu določimo ikono, ki jo vidi uporabnik, glede na to, ali je to mapa, datoteka
    // ali glasbena datoteka. Uporabnik vidi tudi ime datoteke.
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        val fileName = file.name

        holder.fileName.text = fileName
        if (file.isDirectory) {
            holder.subFiles.visibility = View.VISIBLE
            val count = file.listFiles().size
            holder.subFiles.text = context.resources.getQuantityString(
                    R.plurals.numberOfFiles, count, count)
        } else {
            holder.subFiles.visibility = View.GONE
        }


        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder_black_24dp)
        } else if (fileName.endsWith("mp3") || fileName.endsWith("flac")) {
            holder.fileIcon.setImageResource(R.drawable.ic_music_note_black_24dp)
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file_black_24dp)
        }

    }

    override fun getItemCount(): Int {
        return files.size
    }

}