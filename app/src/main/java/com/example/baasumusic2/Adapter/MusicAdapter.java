package com.example.baasumusic2.Adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.baasumusic2.Activity.PlayerActivity;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{
    private Context context;
    public static ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles> musicFiles) {
        this.context = context;
        this.mFiles = musicFiles;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items,parent,false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if(image!=null){
            Glide.with(context).asBitmap().load(image).into(holder.album_art);
        }else{
            Glide.with(context).load(R.drawable.ic_placeholder_album).into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        holder.menu_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,v);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete:
                                Toast.makeText(context, "Deleted Songs", Toast.LENGTH_SHORT).show();
                                deleteFiles(position,v);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    private void deleteFiles(int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId()));
        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted){
            context.getContentResolver().delete(contentUri,null,null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,mFiles.size());
            Snackbar.make(v,"File Deleted: ",Snackbar.LENGTH_LONG).show();
        }else{
            Snackbar.make(v,"Can't Delete File : ",Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView file_name;
        ImageView album_art, menu_more;
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menu_more = itemView.findViewById(R.id.menu_more);
        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try{
            retriever.setDataSource(uri.toString());
            byte[] art = retriever.getEmbeddedPicture();
            return art;
        }catch (Exception e){
            e.printStackTrace(); return null;}
    }
   public void updateList(ArrayList<MusicFiles>musicFilesArrayList){
       mFiles = new ArrayList<>();
       mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
