package com.example.im.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Im on 04-12-2017.
 */

public class MyService extends Service {
    public static MediaPlayer player;
    ArrayList songPath = new ArrayList<>();
    ArrayList songName = new ArrayList<>();
    int position;
    int duration;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    //Starts Service in Background.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);  //checks is App is closed and let the service still running.
        if (intent != null && intent.getExtras() != null) {
            if (player != null)
                player.stop();
            //Extracting data from Intent.
            Bundle bundle = intent.getExtras();
            songPath = bundle.getStringArrayList("SongList");
            position = bundle.getInt("position");
            songName = bundle.getStringArrayList("SongName");
            Uri sing = Uri.parse((String) songPath.get(position)); //Conveting String path into Uri.
            player = MediaPlayer.create(this, sing);
            player.start();  //playing Song Using MediaPlayer.
            //checks if the Songs is over(Here we play next Song if previous is over).
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer play) {
                    position++;
                    if (position >= songPath.size())  //checks if it's last Song in the list.
                        position = 0;
                    if (player != null)
                        player.stop();
                    Uri sing = Uri.parse((String) songPath.get(position));
                    player = MediaPlayer.create(MyService.this, sing);
                    MainActivity.createNotification(getApplicationContext(), (String) songName.get(position));  //shows Notification each time new song is played.
                    player.setOnCompletionListener(this);
                    player.start();
                }
            });
        }
        return START_STICKY;
    }

    //In case if App is closed then also service should keep running in background.
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), this.getClass());
        intent.setPackage(getPackageName());
        startService(intent);
        super.onTaskRemoved(rootIntent);
    }

    //Stops Service(In this case stops playing soogs).
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null)
            player.stop();
    }

    //Pauses Current Song.
    public static void pause() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    //Stops Current Song.
    public static void play() {
        if (!player.isPlaying()) {
            player.start();
        }
    }
}
