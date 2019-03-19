package com.github.adrijanrogan.etiketa.util

import java.io.File
import java.util.Comparator

class FileComparator : Comparator<File> {

    /* Datoteke razvrsti tako, da so na vrhu mape, spodaj pa datoteke, obe skupini
       pa sta sortirani še padajoce po imenu. */
    override fun compare(o1: File, o2: File): Int {
        return when {
            o1.isDirectory == o2.isDirectory ->
                o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
            o1.isDirectory -> -1
            else -> 1
        }
    }
}