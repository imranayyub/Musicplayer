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
    public MediaPlayer player;
    String song;
    ArrayList songs = new ArrayList<>();
    int positions;
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
            song = bundle.getString("Song");

//            songs=bundle.getStringArrayList("SongList");
//            String position=bundle.getString("position");
//             positions=Integer.parseInt(position);
            Uri sing = Uri.parse(song);
            player = MediaPlayer.create(this, sing);
            player.setLooping(true);
            player.start();
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer player) {
//                    onNextClicked(mp);
//                    positions++;
//                    Uri sing = Uri.parse((String) songs.get(positions));
//                    player = MediaPlayer.create(MyService.this, sing);
//            player.setLooping(true);
//                    player.start();
//                }
//            });
        }
        return START_STICKY;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent=new Intent(getApplicationContext(),this.getClass());
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
}
