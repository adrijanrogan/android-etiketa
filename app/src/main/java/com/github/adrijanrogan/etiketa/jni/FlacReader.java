package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

public class FlacReader implements Reader {

    private String path;

    public FlacReader(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    @Override
    public int checkMetadata() {
        if (hasXiphComment(path)) {
            return Reader.METADATA_XIPH_COMMENT;
        } else {
            return Reader.NO_VALID_METADATA;
        }
    }

    @Override
    public Metadata getMetadata() {
        return readXiphComment(path);
    }

    private native boolean hasXiphComment(String filename);
    private native Metadata readXiphComment(String filename);

}
