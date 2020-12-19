package com.example.baasumusic2.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.baasumusic2.Adapter.AlbumAdapter;
import com.example.baasumusic2.Adapter.AlbumDetailAdapter;
import com.example.baasumusic2.Adapter.MusicAdapter;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.R;

import java.util.ArrayList;

import static com.example.baasumusic2.MainActivity.musicFiles;

public class AlbumDetail extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView imageView;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailAdapter albumDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        recyclerView = findViewById(R.id.recyclerView);
        imageView = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");
        int j=0;
        for(int i=0; i<musicFiles.size();i++){
            if(albumName.equals(musicFiles.get(i).getAlbum())){
                albumSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
        // For album
        byte[] image = getAlbumArt(albumSongs.get(0).getPath());
        if (image!=null){
            Glide.with(this).load(image).into(imageView);
        }else{
            Glide.with(this).load(R.drawable.ic_placeholder_album).into(imageView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(albumSongs.size()<1)){
            albumDetailAdapter = new AlbumDetailAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
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