package com.example.baasumusic2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.baasumusic2.Activity.AlbumDetail;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.R;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>{
    private Context context;
    private ArrayList<MusicFiles> albumFiles;

    public AlbumAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.textView.setText(albumFiles.get(position).getAlbum());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if(image!=null){
            Glide.with(context).asBitmap().load(image).into(holder.imageView);
        }else{
            Glide.with(context).load(R.drawable.ic_placeholder_album).into(holder.imageView);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumDetail.class);
                intent.putExtra("albumName",albumFiles.get(position).getAlbum());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView; TextView textView;
        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.album_image);
            textView = itemView.findViewById(R.id.album_name);
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
}
