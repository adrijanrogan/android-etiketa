package com.github.adrijanrogan.etiketa.jni

interface Reader {

    companion object {
        const val NO_VALID_METADATA = -1

        const val METADATA_ID3v1 = 1
        const val METADATA_ID3v2 = 2

        const val METADATA_XIPH_COMMENT = 10
    }

    fun checkMetadata(): Int

    fun getMetadata(): Metadata
}