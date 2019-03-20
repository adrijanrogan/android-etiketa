package com.github.adrijanrogan.etiketa.ui.browse

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.adrijanrogan.etiketa.R
import java.io.File

class BrowseTreeAdapter(private val context: Context) :
        ListAdapter<File, BrowseTreeAdapter.TreeNodeViewHolder>(DIFF_CALLBACK) {


    inner class TreeNodeViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var nodeName: TextView = root.findViewById(R.id.holder_tree_node_name)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeNodeViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(
                R.layout.holder_browser_tree_node, parent, false)
        return TreeNodeViewHolder(root)
    }


    override fun onBindViewHolder(holder: TreeNodeViewHolder, position: Int) {
        if (position == 0) holder.nodeName.text = context.getString(R.string.internal_storage)
        else {
            val file = getItem(position)
            val fileName = file.name
            holder.nodeName.text = fileName
        }
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