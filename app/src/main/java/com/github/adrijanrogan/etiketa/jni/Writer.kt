package com.github.adrijanrogan.etiketa.jni

interface Writer {

    companion object {
        const val METADATA_WRITE_FAILURE = 0
        const val METADATA_WRITE_SUCCESS = 1
    }

    fun writeMetadata(metadata: Metadata): Int
}