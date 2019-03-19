package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

public class Mp3Writer {

    private String path;

    public Mp3Writer(@NonNull String path) {
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
        return writeId3Tag(path, title, artist, album, year, mimeType, imageData);
    }

    private native int writeId3Tag(String filename, String title, String artist,
                                        String album, int year, String mimeType, byte[] jArray);
}
