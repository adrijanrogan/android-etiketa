package com.github.adrijanrogan.etiketa.jni;

import android.support.annotation.NonNull;

public class FlacReader {

    private String filename;

    public FlacReader(@NonNull String filename) {
        this.filename = filename;
        System.loadLibrary("taglib");
    }

    public boolean hasXiphComment() {
        return hasXiphComment(filename);
    }

    public Metadata getMetadata() {
        return readXiphComment(filename);
    }

    private native boolean hasXiphComment(String filename);
    private native Metadata readXiphComment(String filename);

}
