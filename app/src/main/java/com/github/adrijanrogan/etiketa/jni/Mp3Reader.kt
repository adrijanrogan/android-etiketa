package com.github.adrijanrogan.etiketa.jni

class Mp3Reader(private val path: String) : Reader {

    init {
        System.loadLibrary("taglib")
    }

    override fun checkMetadata(): Int {
        return when (hasId3Tag(path)) {
            Reader.METADATA_ID3v1 -> Reader.METADATA_ID3v1
            Reader.METADATA_ID3v2 -> Reader.METADATA_ID3v2
            else -> Reader.NO_VALID_METADATA
        }
    }

    override fun getMetadata(): Metadata {
        return readId3Tag(path)
    }

    private external fun hasId3Tag(filename: String): Int
    private external fun readId3Tag(filename: String): Metadata
}