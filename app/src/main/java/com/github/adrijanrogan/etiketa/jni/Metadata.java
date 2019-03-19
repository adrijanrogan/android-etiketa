package com.github.adrijanrogan.etiketa.jni;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Metadata {

    private String title;
    private String artist;
    private String album;
    private int releaseYear;

    private String imageMimeType;
    private byte[] imageData;
    private String imagePath;

    private int id3Version = 0;

    public Metadata(String title, String artist, String album, int releaseYear,
                    String imageMimeType, byte[] imageData) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.releaseYear = releaseYear;
        this.imageMimeType = imageMimeType;
        this.imageData = imageData;
    }


    public void writeImageToDisk(Context context) {
        if (imageData != null) {
            File folder = context.getDir("pictures", Context.MODE_PRIVATE);
            File image = new File(folder, title + artist);
            try {
                FileOutputStream stream = new FileOutputStream(image);
                stream.write(imageData);
                imagePath = image.getAbsolutePath();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("TITLE", title);
        bundle.putString("ARTIST", artist);
        bundle.putString("ALBUM", album);
        bundle.putInt("YEAR", releaseYear);
        bundle.putString("IMAGE_MIME", imageMimeType);
        bundle.putString("IMAGE_PATH", imagePath);
        bundle.putInt("ID3", id3Version);
        return bundle;
    }

    public void setId3Version(int version) {
        id3Version = version;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getId3Version() {
        return id3Version;
    }
}
