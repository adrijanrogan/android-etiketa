package com.github.adrijanrogan.etiketa.jni;

import android.support.annotation.NonNull;

public class Mp3Reader {

    private String filename;

    public Mp3Reader(@NonNull String filename) {
        this.filename = filename;
    }

    public int getFieldCount() {
        return getFieldCount(filename);
    }

    public Metadata getMetadata() {
        return readId3Tag(filename);
    }

    private native int getFieldCount(String filename);
    private native Metadata readId3Tag(String filename);
}
