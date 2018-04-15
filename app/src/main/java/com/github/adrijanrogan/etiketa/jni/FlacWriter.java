package com.github.adrijanrogan.etiketa.jni;

import android.support.annotation.NonNull;

public class FlacWriter {

    private String filename;

    public FlacWriter(@NonNull String filename) {
        this.filename = filename;
        System.loadLibrary("taglib");
    }

    public int setMetadata(Metadata metadata) {
        String title = metadata.getTitle();
        String artist = metadata.getArtist();
        String album = metadata.getAlbum();
        int year = metadata.getReleaseYear();
        String mimeType = metadata.getImageMimeType();
        byte[] imageData = metadata.getImageData();
        return writeXiphComment(filename, title, artist, album, year, mimeType, imageData);
    }

    private native int writeXiphComment(String filename, String title, String artist,
                                             String album, int year, String mimeType, byte[] jArray);
}
