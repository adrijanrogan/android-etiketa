package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

public class Mp3Reader implements Reader {

    private String path;

    public Mp3Reader(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    @Override
    public int checkMetadata() {
        int v = hasId3Tag(path);
        switch (v) {
            case Reader.METADATA_ID3v1:
                return Reader.METADATA_ID3v1;
            case Reader.METADATA_ID3v2:
                return Reader.METADATA_ID3v2;
            default:
                return Reader.NO_VALID_METADATA;
        }
    }

    @Override
    public Metadata getMetadata() {
        return readId3Tag(path);
    }


    private native int hasId3Tag(String filename);
    private native Metadata readId3Tag(String filename);
}
