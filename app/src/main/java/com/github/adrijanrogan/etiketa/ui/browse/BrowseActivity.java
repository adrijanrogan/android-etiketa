package com.github.adrijanrogan.etiketa.ui.browse;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.adrijanrogan.etiketa.jni.FlacReader;
import com.github.adrijanrogan.etiketa.jni.Metadata;
import com.github.adrijanrogan.etiketa.jni.Mp3Reader;
import com.github.adrijanrogan.etiketa.ui.MetadataActivity;
import com.github.adrijanrogan.etiketa.R;

import java.io.File;

public class BrowseActivity extends AppCompatActivity implements AdapterCallback {

    private BrowseViewModel viewModel;

    private boolean showHidden; // Ce false, skrijemo datoteke z zacetnico "."

    private RecyclerView recyclerView;
    private TextView noFiles;

    // Vstopna tocka v BrowseActivity.
    // Dolocimo postavitev, ki jo zelimo pokazati uporabniku (activity_browser).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        viewModel = ViewModelProviders.of(this).get(BrowseViewModel.class);

        showHidden = false;
        recyclerView = findViewById(R.id.recycler);
        noFiles = findViewById(R.id.text_no_files);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        updateUI();
    }

    // Klik na mapo -> pokazemo novo hierarhijo,
    // sicer -> ce je datoteka podprta, omogocimo spreminjanje metapodatkov te datoteke.
    @Override
    public void onClickFile(int position) {
        File[] currentChildren = viewModel.getChildren();
        if (position <= currentChildren.length) {
            File file = currentChildren[position];
            if (file.isDirectory()) {
                viewModel.goDown(file);
                updateUI();
            } else {
                checkFile(file);
            }
        }
    }

    private void updateUI() {
        viewModel.sortFiles();
        viewModel.removeHiddenFiles();
        File[] currentChildren = viewModel.getChildren();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(viewModel.getTitle(this));
        }

        if (currentChildren.length == 0) {
            recyclerView.setVisibility(View.GONE);
            noFiles.setVisibility(View.VISIBLE);
        } else {
            noFiles.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            BrowseAdapter adapter = new BrowseAdapter(currentChildren, this);
            recyclerView.swapAdapter(adapter, true);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.checkIfParentRoot()) {
            super.onBackPressed();
        } else {
            viewModel.goUp();
            updateUI();
        }
    }


    private void checkFile(File file) {
        // Morda je datoteka medtem bila izbrisana ali premaknjena.
        if (!file.exists()) {
            viewModel.goUp();
            updateUI();
            return;
        }

        String path = file.getAbsolutePath();
        Metadata metadata;

        // Preveri, ali je datoteka mp3 ali flac, saj uporabljata razlicen nacin zapisovanja
        // metapodatkov. Mozno je tudi, da je datoteka poskodovana ali pa sploh ni tega formata.
        // Za vse druge formate datotek uporabniku javimo, da format ni podprt.

        // Vrne vse od zadnje pike naprej.
        // Primer: path = "home/adrijan/foo.mp3" --> format = ".mp3"
        String format = path.substring(path.lastIndexOf("."));
        switch (format) {
            case ".mp3":
                Mp3Reader mp3Reader = new Mp3Reader(path);
                switch (mp3Reader.hasId3Tag()) {
                    case 0:
                        Toast.makeText(this, "Te datoteke ni bilo možno prebrati.",
                                Toast.LENGTH_LONG).show();
                        break;
                    // Za ID3 verzija 1.
                    case 1:
                        metadata = mp3Reader.getMetadata();
                        metadata.setId3Version(1);
                        runActivity(file, metadata);
                        break;
                        // Za ID3 verzija 2.
                    case 2:
                        metadata = mp3Reader.getMetadata();
                        metadata.setId3Version(2);
                        runActivity(file, metadata);
                        break;
                }
                break;
            case ".flac":
                FlacReader flacReader = new FlacReader(path);
                if (!flacReader.hasXiphComment()) {
                    Toast.makeText(this, "Te datoteke ni bilo možno prebrati.",
                            Toast.LENGTH_LONG).show();
                } else {
                    metadata = flacReader.getMetadata();
                    runActivity(file, metadata);
                }
                break;
            default:
                Toast.makeText(this, "Format te datoteke ni podprt.",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void runActivity(File file, Metadata metadata) {
        viewModel.setSelectedFile(file);
        metadata.writeImageToDisk(this);
        Bundle bundle = metadata.toBundle();
        Intent intent = new Intent(this, MetadataActivity.class);
        intent.putExtra("METADATA", bundle);
        intent.putExtra("FILE", file);
        startActivity(intent);
    }
}
