package com.github.adrijanrogan.etiketa.jni;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class Mp3Writer implements Writer {

    private String path;

    public Mp3Writer(@NonNull String path) {
        this.path = path;
        System.loadLibrary("taglib");
    }

    @Override
    public int writeMetadata(@NotNull Metadata metadata) {
        String title = metadata.getTitle();
        String artist = metadata.getArtist();
        String album = metadata.getAlbum();
        int year = metadata.getReleaseYear();
        String mimeType = metadata.getImageMimeType();
        byte[] imageData = metadata.getImageData();
        if (writeId3Tag(path, title, artist, album, year, mimeType, imageData) == 1) {
            return Writer.METADATA_WRITE_SUCCESS;
        } else return Writer.METADATA_WRITE_FAILURE;
    }

    private native int writeId3Tag(String filename, String title, String artist,
                                        String album, int year, String mimeType, byte[] jArray);
}
