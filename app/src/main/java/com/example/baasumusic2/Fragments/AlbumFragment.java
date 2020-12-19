package com.example.baasumusic2.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baasumusic2.Adapter.AlbumAdapter;
import com.example.baasumusic2.Adapter.MusicAdapter;
import com.example.baasumusic2.R;

import static com.example.baasumusic2.MainActivity.albums;
import static com.example.baasumusic2.MainActivity.musicFiles;

public class AlbumFragment extends Fragment {
    private View view;
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;

    public AlbumFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.albumRv);
        recyclerView.setHasFixedSize(true);
        if(!(albums.size()<1)){
            albumAdapter = new AlbumAdapter(getContext(),albums);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
            recyclerView.setAdapter(albumAdapter);
        }
        return view;
    }
}