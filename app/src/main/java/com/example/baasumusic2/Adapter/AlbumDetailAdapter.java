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
import com.example.baasumusic2.Activity.PlayerActivity;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.R;

import java.util.ArrayList;

public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.AlbumDetailViewHolder>{
    private Context context;
    public static ArrayList<MusicFiles> albumFiles;

    public AlbumDetailAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items,parent,false);
        return new AlbumDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailViewHolder holder, int position) {
        holder.textView.setText(albumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if(image!=null){
            Glide.with(context).asBitmap().load(image).into(holder.imageView);
        }else{
            Glide.with(context).load(R.drawable.ic_placeholder_album).into(holder.imageView);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender","albumDetails");
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class AlbumDetailViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public AlbumDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.music_img);
            textView = itemView.findViewById(R.id.music_file_name);

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
