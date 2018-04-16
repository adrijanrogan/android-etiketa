package com.github.adrijanrogan.etiketa.jni;

import android.support.annotation.NonNull;

public class FlacWriter {

    private String path;

    // Nalozimo knjiznico in shranimo ime datoteke.
    public FlacWriter(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    public int setMetadata(Metadata metadata) {
        String title = metadata.getTitle();
        String artist = metadata.getArtist();
        String album = metadata.getAlbum();
        int year = metadata.getReleaseYear();
        String mimeType = metadata.getImageMimeType();
        byte[] imageData = metadata.getImageData();
        return writeXiphComment(path, title, artist, album, year, mimeType, imageData);
    }

    // Nativna metoda.
    private native int writeXiphComment(String filename, String title, String artist,
                                             String album, int year, String mimeType, byte[] jArray);
}
