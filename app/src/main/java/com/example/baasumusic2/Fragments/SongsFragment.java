package com.example.baasumusic2.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.baasumusic2.Adapter.MusicAdapter;
import com.example.baasumusic2.R;

import static com.example.baasumusic2.Adapter.MusicAdapter.mFiles;
import static com.example.baasumusic2.MainActivity.musicFiles;

public class SongsFragment extends Fragment {
    private View view;
    RecyclerView recyclerView;
    public static MusicAdapter musicAdapter;
    public SongsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(!(musicFiles.size()<1)){
            musicAdapter = new MusicAdapter(getContext(),musicFiles);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
            recyclerView.setAdapter(musicAdapter);
        }
        return view;
    }
}