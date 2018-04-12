package com.github.adrijanrogan.etiketa.ui;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.github.adrijanrogan.etiketa.jni.FlacReader;
import com.github.adrijanrogan.etiketa.jni.Metadata;
import com.github.adrijanrogan.etiketa.jni.Mp3Reader;
import com.github.adrijanrogan.etiketa.util.FileComparator;
import com.github.adrijanrogan.etiketa.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowserActivity extends AppCompatActivity implements AdapterCallback {

    // Vedno hranimo referenco parent, da se lahko enostavneje vrnemo v visjo hierarhijo.
    // Ce sta parent in root enaka, smo ze najvisje.
    // Uporabnik se v visjo hierarhijo vraca s tipko nazaj, v primeru, da smo ze najvisje,
    // pa se aplikacija (kot je obicajno za tipko nazaj) zapre.
    private File root;
    private File parent;
    private File[] children;
    private boolean showHidden; // Ce false, skrijemo datoteke z zacetnico "."

    private RecyclerView recyclerView;

    // Vstopna tocka v BrowserActivity.
    // Dolocimo postavitev, ki jo zelimo pokazati uporabniku (activity_browser).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        showHidden = false;
        recyclerView = findViewById(R.id.recycler);
        getRootFile();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        updateRecyclerView();
    }

    // Pridobimo zacetno tocko uporabniskega spomina in datoteke pod zacetno tocko.
    private void getRootFile() {
        root = new File(Environment.getExternalStorageDirectory().getPath() + "/");
        parent = root;
        children = root.listFiles();
        if (!showHidden) {
            removeHiddenFiles();
        }
        sortChildren();
    }

    // Povratni klic, ki ga prejmemo, ko uporabnik klikne na element v seznamu RecyclerView.
    // Ce je kliknil na mapo, pokazemo novo hierarhijo, sicer pa, ce je datoteka podprta,
    // omogocimo spreminjanje metapodatkov te datoteke.
    @Override
    public void onClickFile(int position) {
        if (position <= children.length) {
            File file = children[position];
            if (file.isDirectory()) {
                parent = children[position];
                children = parent.listFiles();
                if (!showHidden) {
                    removeHiddenFiles();
                }
                sortChildren();
                updateRecyclerView();
            } else {
                checkFile(file);
            }
        }
    }

    // Ob spremembi datotek posodobimo RecylcerView.
    private void updateRecyclerView() {
        BrowserAdapter adapter = new BrowserAdapter(children, this);
        recyclerView.swapAdapter(adapter, true);
    }

    // Povratni klic, ko uporabnik pritisne tipko nazaj.
    @Override
    public void onBackPressed() {
        if (parent.getAbsolutePath().equals(root.getAbsolutePath())) {
            // Po navadi poskrbi, da se vrnemo v prejsnji Activity. Ker prejsnjega
            // Activity ni, se aplikacija zapre.
            super.onBackPressed();
        } else {
            parent = parent.getParentFile();
            children = parent.listFiles();
            if (!showHidden) {
                removeHiddenFiles();
            }
            sortChildren();
            updateRecyclerView();
        }
    }

    // Sortiramo datoteke po tipu (mapa ali datoteka) in imenu
    private void sortChildren() {
        Arrays.sort(children, new FileComparator());
    }

    // Odstranimo datoteke, ki se zacnejo na "." (skrite datoteke)
    private void removeHiddenFiles() {
        List<File> fileList = new ArrayList<>(Arrays.asList(children));
        for (int i = fileList.size() - 1; i >= 0; i--) {
            File file = fileList.get(i);
            if (file.getName().startsWith(".")) {
                fileList.remove(i);
            }
        }
        children = fileList.toArray(new File[0]);
    }

    // Preveri, ce je glasbena datoteka. Ce je, odpre MetadataActivity za spreminjanje
    // metapodatkov.
    private void checkFile(File file) {
        String filename = file.getName();
        Toast.makeText(this, filename, Toast.LENGTH_LONG).show();
        if (filename.endsWith(".mp3")) {
            Mp3Reader mp3Reader = new Mp3Reader(filename);
            Metadata metadata = mp3Reader.getMetadata();
            Bundle bundle = metadata.toBundle();
            Intent intent = new Intent(this, MetadataActivity.class);
            intent.putExtra("METADATA", bundle);
            intent.putExtra("FILENAME", filename);
            startActivity(intent);
        } else if (filename.endsWith(".flac")) {
            FlacReader flacReader = new FlacReader(filename);
            if (!flacReader.hasXiphComment()) {
                Toast.makeText(this, "Datoteke ni bilo možno prebrati", Toast.LENGTH_LONG).show();
            } else {
                Metadata metadata = flacReader.getMetadata();
                Bundle bundle = metadata.toBundle();
                Intent intent = new Intent(this, MetadataActivity.class);
                intent.putExtra("METADATA", bundle);
                intent.putExtra("FILENAME", filename);
                startActivity(intent);
            }
        } else {
            //Toast.makeText(this, "Format te datoteke ni podprt.", Toast.LENGTH_LONG).show();
        }
    }
}
