package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

public class FlacReader {

    private String path;

    public FlacReader(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    public boolean hasXiphComment() {
        return hasXiphComment(path);
    }

    public Metadata getMetadata() {
        return readXiphComment(path);
    }

    private native boolean hasXiphComment(String filename);
    private native Metadata readXiphComment(String filename);

}
