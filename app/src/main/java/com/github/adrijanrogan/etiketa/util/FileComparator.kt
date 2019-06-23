package com.github.adrijanrogan.etiketa.util

import java.io.File
import java.util.*

class FileComparator(private val sortMode: Int) : Comparator<File> {

    override fun compare(o1: File, o2: File): Int {
        val type = sortMode or SORT_MODE_FILTER
        val asc = sortMode and SORT_MODE_REVERSED == 0
        val ascending = if (asc) 1 else -1
        val group = sortMode and SORT_MODE_DO_NOT_GROUP == 0

        val result = when (type) {
            SORT_MODE_FILENAME -> compareByName(o1, o2, group)
            else -> compareByName(o1, o2, group)
        }

        return ascending * result
    }

    private fun compareByName(first: File, second: File, group: Boolean): Int {
        return if (group) {
            when {
                first.isDirectory == second.isDirectory -> compareByName(first, second, false)
                first.isDirectory -> -1
                else -> 1
            }
        } else {
            first.name.toLowerCase().compareTo(second.name.toLowerCase())
        }
    }


    companion object {

        // "First" bit:
        // 0: sort by filename
        // 1: sort by last modified
        const val SORT_MODE_FILENAME = 0x000
        const val SORT_MODE_LAST_MODIFIED = 0x100

        const val SORT_MODE_FILTER = 0x100

        // "Second" bit:
        // 0: ascending (normal)
        // 1: descending (reversed)
        const val SORT_MODE_NORMAL = 0x000
        const val SORT_MODE_REVERSED = 0x010

        // "Third" bit:
        // 0: group folders and files
        // 1: mixed folders and files
        const val SORT_MODE_GROUP = 0x000
        const val SORT_MODE_DO_NOT_GROUP = 0x001

        // TODO: Group by extension option
    }
}