package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

public class Mp3Reader {

    private String path;

    public Mp3Reader(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    public int hasId3Tag() {
        return hasId3Tag(path);
    }

    public Metadata getMetadata() {
        return readId3Tag(path);
    }

    private native int hasId3Tag(String filename);
    private native Metadata readId3Tag(String filename);
}
