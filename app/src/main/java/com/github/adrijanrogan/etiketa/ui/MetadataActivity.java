package com.github.adrijanrogan.etiketa.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.adrijanrogan.etiketa.R;
import com.github.adrijanrogan.etiketa.jni.FlacWriter;
import com.github.adrijanrogan.etiketa.jni.Metadata;
import com.github.adrijanrogan.etiketa.jni.Mp3Writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

public class MetadataActivity extends AppCompatActivity {

    private Bundle metadata;
    private File file;
    private String filename;
    private String path;
    private String imagePath;
    private Context context;

    private boolean imageChanged;

    private String title, artist, album;
    private int year, id3Version;

    private ImageView imageView;
    private EditText titleEdit;
    private EditText artistEdit;
    private EditText albumEdit;
    private EditText yearEdit;
    private Button buttonSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);
        // Iz intenta dobimo Bundle, ki hrani metapodatke, in objekt File.
        metadata = getIntent().getBundleExtra("METADATA");
        file = (File) getIntent().getSerializableExtra("FILE");
        filename = file.getName();
        path = file.getAbsolutePath();
        context = this;

        imagePath = metadata.getString("IMAGE_PATH");
        title = metadata.getString("TITLE");
        artist = metadata.getString("ARTIST");
        album = metadata.getString("ALBUM");
        year = metadata.getInt("YEAR");

        // Poiscemo nase komponente uporabniskega vmesnika.
        imageView = findViewById(R.id.image);
        titleEdit = findViewById(R.id.text_title);
        artistEdit = findViewById(R.id.text_artist);
        albumEdit = findViewById(R.id.text_album);
        yearEdit = findViewById(R.id.text_year);
        buttonSave = findViewById(R.id.button_save);

        // Kot naslov Activity damo kar ime datoteke.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(filename);
        }

        // Vstavimo sliko, ce obstaja. Sicer skrijemo sliko in zapisemo razlog.
        if (imagePath != null) {
            imageView.setVisibility(View.VISIBLE);
            Bitmap albumArt = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(albumArt);
        } else if (filename.endsWith(".mp3") && metadata.getInt("ID3") == 1) {
            TextView textImage = findViewById(R.id.text_image);
            textImage.setText(R.string.id3_starejsi_format);
            imageView.setVisibility(View.GONE);
            textImage.setVisibility(View.VISIBLE);
        } else {
            TextView textImage = findViewById(R.id.text_image);
            textImage.setText(R.string.slika_ni_najdena);
            imageView.setVisibility(View.GONE);
            textImage.setVisibility(View.VISIBLE);
        }


        titleEdit.setText(title);
        artistEdit.setText(artist);
        albumEdit.setText(album);
        yearEdit.setText(String.valueOf(year));

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (compareData()) {
                    Metadata metadata = makeMetadata();
                    if (filename.endsWith(".mp3")) {
                        Mp3Writer mp3Writer = new Mp3Writer(path);
                        int s = mp3Writer.setMetadata(metadata);
                        postResult(s);
                    } else if (filename.endsWith(".flac")) {
                        FlacWriter flacWriter = new FlacWriter(path);
                        int s = flacWriter.setMetadata(metadata);
                        postResult(s);
                    } else {
                        Toast.makeText(context, "Interna napaka.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    postResult(2);
                }
                finish();
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        });
    }

    private void postResult(int s) {
        switch (s) {
            case 0:
                Toast.makeText(this, "Napaka pri shranjevanju",
                        Toast.LENGTH_LONG).show();
                break;
            case 1:
                Toast.makeText(this, "Metapodatki uspešno shranjeni",
                        Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "Metapodatki so ostali nespremenjeni",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Primerja podatke in ugotovi, ali so bili spremenjeni.
    private boolean compareData() {
        int newYear = 0;
        if (!yearEdit.getText().toString().equals("")) {
            newYear = Integer.valueOf(yearEdit.getText().toString());
        }
        return imageChanged || !titleEdit.getText().toString().equals(title) ||
                !artistEdit.getText().toString().equals(artist) ||
                !albumEdit.getText().toString().equals(album) ||
                newYear != year;
    }

    // Konstruira Metadata, ki se potem lahko zapise v datoteko.
    private Metadata makeMetadata() {
        String title_ = null, artist_ = null, album_ = null, mimeType_ = null;
        int year_ = -1;
        byte[] imageData_ = null;

        if (!titleEdit.getText().toString().equals(title)) {
            title_ = titleEdit.getText().toString();
        }

        if (!artistEdit.getText().toString().equals(artist)) {
            artist_ = artistEdit.getText().toString();
        }

        if (!albumEdit.getText().toString().equals(album)) {
            album_ = albumEdit.getText().toString();
        }

        if (!yearEdit.getText().toString().equals("")) {
            year_ = Integer.valueOf(yearEdit.getText().toString());
        }

        if (imageChanged) {
            Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            imageData_ = outputStream.toByteArray();
            mimeType_ = "image/jpeg";
        }

        return new Metadata(title_, artist_, album_, year_, mimeType_, imageData_);
    }
}
