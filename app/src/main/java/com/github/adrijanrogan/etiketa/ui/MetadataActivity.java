package com.github.adrijanrogan.etiketa.ui;

import android.content.Context;
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
    private Context context;

    private ImageView image;
    private EditText title;
    private EditText artist;
    private EditText album;
    private EditText year;
    private Button buttonSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);
        metadata = getIntent().getBundleExtra("METADATA");
        filename = getIntent().getStringExtra("FILENAME");
        context = this;

        image = findViewById(R.id.image);
        title = findViewById(R.id.text_title);
        artist = findViewById(R.id.text_artist);
        album = findViewById(R.id.text_album);
        year = findViewById(R.id.text_year);
        buttonSave = findViewById(R.id.button_save);

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
