package com.github.adrijanrogan.etiketa.ui.browse;

import android.content.Context;
import android.os.Environment;

import com.github.adrijanrogan.etiketa.R;
import com.github.adrijanrogan.etiketa.util.FileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.ViewModel;

public class BrowseViewModel extends ViewModel {

    // Vedno hranimo referenco parent, da se lahko enostavneje vrnemo v visjo hierarhijo.
    // Ce sta parent in root enaka, smo ze najvisje.
    // Uporabnik se v visjo hierarhijo vraca s tipko nazaj, v primeru, da smo ze najvisje,
    // pa se aplikacija (kot je obicajno za tipko nazaj) zapre.
    private File root;
    private File parent;
    private File[] children;

    public BrowseViewModel() {
        super();
        root = new File(Environment.getExternalStorageDirectory().getPath() + "/");
        parent = root;
        children = parent.listFiles();
    }

    void goUp() {
        parent = parent.getParentFile();
        children = parent.listFiles();
    }

    void goDown(File file) {
        parent = file;
        children = parent.listFiles();
    }

    File[] getChildren() {
        return children;
    }

    boolean checkIfParentRoot() {
        return parent.getAbsolutePath().equals(root.getAbsolutePath());
    }

    String getTitle(Context context) {
        if (checkIfParentRoot()) {
            return context.getString(R.string.internal_storage);
        } else {
            return parent.getName();
        }
    }

    void sortFiles() {
        Arrays.sort(children, new FileComparator());
    }

    void removeHiddenFiles() {
        List<File> fileList = new ArrayList<>(Arrays.asList(children));
        for (int i = fileList.size() - 1; i >= 0; i--) {
            File file = fileList.get(i);
            if (file.getName().startsWith(".")) {
                fileList.remove(i);
            }
        }
        children = fileList.toArray(new File[0]);
    }
}
