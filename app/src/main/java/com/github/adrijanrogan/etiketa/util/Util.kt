package com.github.adrijanrogan.etiketa.util

fun getFileSize(length: Long): Pair<Double, String> {
    var size = length.toDouble()
    return if (size < 1024) Pair(size, "B")
    else {
        size /= 1024
        if (size < 1024) Pair(size, "kB")
        else {
            size /= 1024
            if (size < 1024) Pair(size, "MB")
            else Pair(size/1024, "GB")
        }
    }
}