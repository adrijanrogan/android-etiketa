package com.github.adrijanrogan.etiketa.jni

class FlacReader(private val path: String) : Reader {

    init {
        System.loadLibrary("taglib")
    }

    override fun checkMetadata(): Int {
        return if (hasXiphComment(path)) {
            Reader.METADATA_XIPH_COMMENT
        } else {
            Reader.NO_VALID_METADATA
        }
    }

    override fun getMetadata(): Metadata {
        return readXiphComment(path)
    }

    private external fun hasXiphComment(filename: String): Boolean
    private external fun readXiphComment(filename: String): Metadata

}