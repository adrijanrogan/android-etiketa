package com.github.adrijanrogan.etiketa.jni;

import android.os.Bundle;

// Razred, ki vsebuje metapodatke o glasbenih datotekah.
public class Metadata {

    private String title; // Naslov skladbe.
    private String artist; // Izvajalec skladbe.
    private String album; // Album skladbe.
    private int releaseYear; // Leto izdaje skladbe.

    private String imageMimeType; // MIME tip slike.
    private byte[] imageData; // Podatki slike.

    public Metadata(String title, String artist, String album, int releaseYear,
                    String imageMimeType, byte[] imageData) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.releaseYear = releaseYear;
        this.imageMimeType = imageMimeType;
        this.imageData = imageData;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("TITLE", title);
        bundle.putString("ARTIST", artist);
        bundle.putString("ALBUM", album);
        bundle.putInt("YEAR", releaseYear);
        bundle.putString("IMAGE_MIME", imageMimeType);
        bundle.putByteArray("IMAGE_DATA", imageData);
        return bundle;
    }
}
