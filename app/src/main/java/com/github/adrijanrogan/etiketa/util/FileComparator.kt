package com.github.adrijanrogan.etiketa.util

import java.io.File
import java.util.*

class FileComparator(private val comparisonType: Int) : Comparator<File> {

    companion object {
        // First folder names a-z, then file names a-z
        const val COMPARISON_FOLDER_NAME = 0
        // First file names a-z, then folder names a-z
        const val COMPARSION_FOLDER_REVERSED_NAME = 2
        // First folder names z-a, then file names z-a
        const val COMPARISON_FOLDER_NAME_REVERSED = 1
        // First file names z-a, then folder names z-a
        const val COMPARSION_FOLDER_REVERSED_NAME_REVERSED = 2

        // Folders and files mixed a-z
        const val COMPARISON_NAME = 10
        // Folders and files mixed z-a
        const val COMPARISON_NAME_REVERSED = 11

        // First group by extension, then sort by name a-z in groups
        const val COMPARISON_EXTENSION_NAME = 20
    }

    /* Datoteke razvrsti tako, da so na vrhu mape, spodaj pa datoteke, obe skupini
       pa sta sortirani še padajoce po imenu. */
    override fun compare(o1: File, o2: File): Int {
        return when (comparisonType) {
            COMPARISON_FOLDER_NAME -> compareByFolderAndName(o1, o2)
            else -> compareByFolderAndName(o1, o2)
        }
    }

    private fun compareByFolderAndName(o1: File, o2: File): Int {
        return when {
            o1.isDirectory == o2.isDirectory ->
                o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
            o1.isDirectory -> -1
            else -> 1
        }
    }
}