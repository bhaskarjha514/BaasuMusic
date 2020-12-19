package com.example.baasumusic2.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.baasumusic2.Interface.ActionPlaying;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.Notification.NotificationReceiver;
import com.example.baasumusic2.R;
import com.example.baasumusic2.Service.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.baasumusic2.Adapter.AlbumDetailAdapter.albumFiles;
import static com.example.baasumusic2.Adapter.MusicAdapter.mFiles;
import static com.example.baasumusic2.MainActivity.musicFiles;
import static com.example.baasumusic2.MainActivity.repeatBoolean;
import static com.example.baasumusic2.MainActivity.shuffleBoolean;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_NEXT;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_PLAY;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_PREVIOUS;
import static com.example.baasumusic2.Service.ApplicationClass.CHANNEL_ID_2;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    static Uri uri;
    int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    //static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initViews();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null && fromUser){
                    musicService.seekTo(progress*1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int currentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(currentPosition);
                    duration_played.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                }else{
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean){
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                }else{
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position",-1);
        String sender = getIntent().getStringExtra("sender");
        if (sender!=null && sender.equals("albumDetails")){
            listSongs = albumFiles;
        }else{
            listSongs = mFiles;
        }
        if(listSongs!=null){
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
//        musicService.showNotification(R.drawable.ic_pause);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);
    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art!=null){
            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if(swatch!=null){
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout relativeLayout = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        relativeLayout.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),swatch.getRgb()});
                        relativeLayout.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }else{
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout relativeLayout = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        relativeLayout.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0xff000000});
                        relativeLayout.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }else{
            Glide.with(this).asBitmap().load(R.drawable.ic_placeholder_album).into(cover_art);
            ImageView gradient = findViewById(R.id.imageViewGradient);
            RelativeLayout relativeLayout = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            relativeLayout.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }
    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animationOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animationOut);
    }
    private String formattedTime(int currentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(currentPosition%60);
        String minutes = String.valueOf(currentPosition/60);
        totalOut = minutes+":"+seconds;
        totalNew = minutes+":"+"0"+seconds;
        if(seconds.length()==1){
            return totalNew;
        }else{
            return totalOut;
        }
    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.playPause);
        seekBar = findViewById(R.id.seekBar);
    }
    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }
    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }
    public void playPauseBtnClicked() {
        if(musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);}
            });
        }else{
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }

    }
    public void nextBtnClicked() {
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }else if (!shuffleBoolean && !repeatBoolean){
                position = ((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);}
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        }else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }else if (!shuffleBoolean && !repeatBoolean){
                position = ((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);}
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }else if (!shuffleBoolean && !repeatBoolean){
                position = ((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);}
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        }else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size()-1);
            }else if (!shuffleBoolean && !repeatBoolean){
                position = ((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int currentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(currentPosition);
                    }
                    handler.postDelayed(this, 1000);}
            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.OnCompleted();
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService  = null;
    }
}