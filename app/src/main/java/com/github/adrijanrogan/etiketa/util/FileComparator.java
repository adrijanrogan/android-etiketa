package com.github.adrijanrogan.etiketa.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        if (o1.isDirectory() == o2.isDirectory()) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        } else if (o1.isDirectory()) {
            return -1;
        } else {
            return 1;
        }
    }
}
