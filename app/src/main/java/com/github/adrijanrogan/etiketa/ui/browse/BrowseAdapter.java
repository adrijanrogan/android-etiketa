package com.github.adrijanrogan.etiketa.ui.browse;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.adrijanrogan.etiketa.R;

import java.io.File;

class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.FileViewHolder> {

    private File[] files;
    private AdapterCallback callback;


    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView fileIcon;
        TextView fileName;
        TextView subFiles;

        FileViewHolder(View root) {
            super(root);
            root.setOnClickListener(this);
            this.fileIcon = root.findViewById(R.id.holder_icon);
            this.fileName = root.findViewById(R.id.holder_file_name);
            this.subFiles = root.findViewById(R.id.holder_sub_files);
        }

        @Override
        public void onClick(View v) {
            callback.onClickFile(getLayoutPosition());
        }
    }



    public BrowseAdapter(File[] files, AdapterCallback callback) {
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

    @Override
    public int getItemCount() {
        return files.length;
    }

}
