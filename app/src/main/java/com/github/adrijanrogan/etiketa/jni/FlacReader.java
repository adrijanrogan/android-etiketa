package com.github.adrijanrogan.etiketa.jni;

import android.support.annotation.NonNull;

public class FlacReader {

    private String path;

    // Nalozimo knjiznico in shranimo ime datoteke.
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

    // Nativni metodi.
    private native boolean hasXiphComment(String filename);
    private native Metadata readXiphComment(String filename);

}
