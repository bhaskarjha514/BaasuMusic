package com.example.baasumusic2.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.baasumusic2.Activity.PlayerActivity;
import com.example.baasumusic2.Interface.ActionPlaying;
import com.example.baasumusic2.Model.MusicFiles;
import com.example.baasumusic2.Notification.NotificationReceiver;
import com.example.baasumusic2.R;

import java.security.Provider;
import java.util.ArrayList;

import static com.example.baasumusic2.Activity.PlayerActivity.listSongs;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_NEXT;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_PLAY;
import static com.example.baasumusic2.Service.ApplicationClass.ACTION_PREVIOUS;
import static com.example.baasumusic2.Service.ApplicationClass.CHANNEL_ID_2;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");
        new CountDownTimer(30*60000, 1000) {

            public void onTick(long millisUntilFinished) {
//                Toast.makeText(getBaseContext(), "Seconds"+millisUntilFinished/1000, Toast.LENGTH_SHORT).show();
//                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                actionPlaying.playPauseBtnClicked();
            }
        }.start();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        position = myPosition;
        if(myPosition!=-1){
            playMedia(myPosition);
        }
        if(actionName!=null){
            switch (actionName){
                case "playPause":
                    if (actionName!=null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                    if (actionName!=null){
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    if (actionName!=null){
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int StartPosition) {
        musicFiles = listSongs;
        position = StartPosition;
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }


    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }
    public void start(){
        mediaPlayer.start();
    }
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    public void stop(){
        mediaPlayer.stop();
    }
    public void release(){
        mediaPlayer.release();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    public void createMediaPlayer(int position){
        this.position = position;
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying!=null){
            actionPlaying.nextBtnClicked();
            if (mediaPlayer!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }
    }
    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
    public void showNotification(int playPauseBtn){
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        Log.d("Position",String.valueOf(position));
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture!=null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else{
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_placeholder_album);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2).setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_prev,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_skip_next,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(1,notification);
//        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0,notification);

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
