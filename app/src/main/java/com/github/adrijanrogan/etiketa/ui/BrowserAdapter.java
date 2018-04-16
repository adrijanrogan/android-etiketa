package com.github.adrijanrogan.etiketa.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.adrijanrogan.etiketa.R;

import java.io.File;

class BrowserAdapter extends RecyclerView.Adapter<BrowserAdapter.FileViewHolder> {

    private File[] files;
    private AdapterCallback callback;

    // Razred FileViewHolder nosi podatke o enem elementu v seznamu.
    // Nas seznam predstavlja polje File[].

    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView fileIcon;
        TextView fileName;
        TextView subFiles;

        FileViewHolder(View root) {
            super(root);
            // Na vsak element v seznamu "polozimo" poslusalca. Ko se uporabnik dotakne
            // enega elementa, dobimo povratni klic v spodnji metodi onClick(View v)
            root.setOnClickListener(this);
            this.fileIcon = root.findViewById(R.id.holder_icon);
            this.fileName = root.findViewById(R.id.holder_file_name);
            this.subFiles = root.findViewById(R.id.holder_sub_files);
        }

        // Preko vmesnika AdapterCallback posljemo povratni klic v BrowserActivity
        @Override
        public void onClick(View v) {
            callback.onClickFile(getLayoutPosition());
        }
    }



    public BrowserAdapter(File[] files, AdapterCallback callback) {
        this.files = files;
        this.callback = callback;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.
                from(parent.getContext()).inflate(R.layout.holder_file, parent, false);
        return new FileViewHolder(root);
    }

    // Ta metoda se pokliče za vsak element v seznamu, ko bo element postal viden.
    // Tu določimo ikono, ki jo vidi uporabnik, glede na to, ali je to mapa, datoteka
    // ali glasbena datoteka. Uporabnik vidi tudi ime datoteke.
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files[position];
        String fileName = file.getName();

        holder.fileName.setText(fileName);
        // Pravilno sklanjamo glede na stevilo datotek v mapi.
        if (file.isDirectory()) {
            holder.subFiles.setVisibility(View.VISIBLE);
            switch (file.listFiles().length) {
                case 1:
                    holder.subFiles.setText("1 datoteka");
                    break;
                case 2:
                    holder.subFiles.setText("2 datoteki");
                    break;
                case 3:
                case 4:
                    holder.subFiles.setText(file.listFiles().length + " datoteke");
                    break;
                default:
                    holder.subFiles.setText(file.listFiles().length + " datotek");
                    break;
            }
        } else {
            holder.subFiles.setVisibility(View.GONE);
        }


        if (file.isDirectory()) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder_black_24dp);
        } else if (fileName.endsWith("mp3") || fileName.endsWith("flac")) {
            holder.fileIcon.setImageResource(R.drawable.ic_music_note_black_24dp);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file_black_24dp);
        }

    }

    // RecyclerView mora vedeti, koliko elementov imamo.
    @Override
    public int getItemCount() {
        return files.length;
    }

}
