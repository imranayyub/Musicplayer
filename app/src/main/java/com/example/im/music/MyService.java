package com.example.im.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.util.ArrayList;

/**
 * Created by Im on 04-12-2017.
 */

public class MyService extends Service {
    public static MediaPlayer player;
    ArrayList songPath = new ArrayList<>();
    ArrayList songName = new ArrayList<>();
    int position;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);

        if (intent != null && intent.getExtras() != null) {
            if (player != null)
                player.stop();
            Bundle bundle = intent.getExtras();
            songPath = bundle.getStringArrayList("SongList");
            position = bundle.getInt("position");
            songName=bundle.getStringArrayList("SongName");
            Uri sing = Uri.parse((String) songPath.get(position));
            player = MediaPlayer.create(this, sing);
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer play) {
                    position++;
                    if(position>=songPath.size())
                        position=0;
                    if (player != null)
                        player.stop();
                    Uri sing = Uri.parse((String) songPath.get(position));
                    player = MediaPlayer.create(MyService.this, sing);
                    MainActivity.customBigNotification(getApplicationContext(), (String) songName.get(position));
                    player.setOnCompletionListener(this);
                    player.start();
                }
            });
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), this.getClass());
        intent.setPackage(getPackageName());
        startService(intent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null)
            player.stop();
    }
    public static void pause()
    {
        if(player.isPlaying())
        {
            player.pause();
        }
    }
    public static void play()
    {
        if(!player.isPlaying())
        {
            player.start();
        }
    }
}
