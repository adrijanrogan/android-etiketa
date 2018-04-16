package com.github.adrijanrogan.etiketa.ui;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
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

    // V prihodnje lahko uporabniku ponudimo izbiro, ali naj aplikacija pokaze tudi
    // skrite datoteke.
    private boolean showHidden; // Ce false, skrijemo datoteke z zacetnico "."

    private View rootView;
    private RecyclerView recyclerView;
    private TextView noFiles;

    // Vstopna tocka v BrowserActivity.
    // Dolocimo postavitev, ki jo zelimo pokazati uporabniku (activity_browser).
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        showHidden = false;
        rootView = findViewById(R.id.root_view);
        recyclerView = findViewById(R.id.recycler);
        noFiles = findViewById(R.id.text_no_files);
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
        // Za vsak slucaj preverimo, ali je pozicija znotraj polja, saj se v nasprotnem
        // primeru aplikacija prisilno zaustavi.
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
        if (parent.getAbsolutePath().equals(root.getAbsolutePath())) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(parent.getName());
            }
        }
        if (children.length == 0) {
            recyclerView.setVisibility(View.GONE);
            noFiles.setVisibility(View.VISIBLE);
        } else {
            noFiles.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            BrowserAdapter adapter = new BrowserAdapter(children, this);
            recyclerView.swapAdapter(adapter, true);
        }

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
        // Morda je datoteka medtem bila izbrisana.
        if (!file.exists()) {
            Toast.makeText(this,
                    "Ta datoteka ne obstaja več. Seznam datotek se je samodejno osvežil",
                    Toast.LENGTH_LONG).show();
            children = parent.listFiles();
            if (!showHidden) {
                removeHiddenFiles();
            }
            sortChildren();
            updateRecyclerView();
            return;
        }
        String path = file.getAbsolutePath();
        Metadata metadata;
        // Preveri, ali je datoteka mp3 ali flac, saj uporabljata razlicen nacin zapisovanja
        // metapodatkov.
        // Mozno je tudi, da je datoteka poskodovana ali pa sploh ni mp3 ali flac formata.
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

    // Odpre MetadataActivity za spreminjanje metapodatkov.
    private void runActivity(File file, Metadata metadata) {
        metadata.writeImageToDisk(this);
        Bundle bundle = metadata.toBundle();
        Intent intent = new Intent(this, MetadataActivity.class);
        intent.putExtra("METADATA", bundle);
        intent.putExtra("FILE", file);
        startActivity(intent);
    }
}
