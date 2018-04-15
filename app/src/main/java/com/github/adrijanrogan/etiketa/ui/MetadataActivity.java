package com.github.adrijanrogan.etiketa.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.adrijanrogan.etiketa.R;
import com.github.adrijanrogan.etiketa.jni.FlacWriter;
import com.github.adrijanrogan.etiketa.jni.Mp3Writer;

public class MetadataActivity extends AppCompatActivity {

    private Bundle metadata;
    private String filename;
    private String imagePath;
    private Context context;

    private String title, artist, album;
    private int year;

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
        metadata = getIntent().getBundleExtra("METADATA");
        filename = getIntent().getStringExtra("FILENAME");
        context = this;

        Bundle metadata = getIntent().getBundleExtra("METADATA");
        imagePath = metadata.getString("IMAGE_PATH");
        title = metadata.getString("TITLE");
        artist = metadata.getString("ARTIST");
        album = metadata.getString("ALBUM");
        year = metadata.getInt("YEAR");

        imageView = findViewById(R.id.image);
        titleEdit = findViewById(R.id.text_title);
        artistEdit = findViewById(R.id.text_artist);
        albumEdit = findViewById(R.id.text_album);
        yearEdit = findViewById(R.id.text_year);
        buttonSave = findViewById(R.id.button_save);

        if (imagePath != null) {
            Bitmap albumArt = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(albumArt);
        }

        titleEdit.setText(title);
        artistEdit.setText(artist);
        albumEdit.setText(album);
        //yearEdit.setText(year);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filename.endsWith(".mp3")) {
                    Mp3Writer mp3Writer = new Mp3Writer();
                } else if (filename.endsWith(".flac")) {
                    FlacWriter flacWriter = new FlacWriter();
                } else {
                    Toast.makeText(context, "Interna napaka.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
