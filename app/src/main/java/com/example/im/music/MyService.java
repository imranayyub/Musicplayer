package com.example.im.music;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import static com.example.im.music.R.layout.big_notification;

/**
 * Created by Im on 04-12-2017.
 */

public class MyService extends Service {
    public static MediaPlayer player;
    ArrayList songPath = new ArrayList<>();
    ArrayList songName = new ArrayList<>();
//        ArrayList songart = new ArrayList<>();
    int position;
//    int duration;
    String albumArt;

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    //Starts Service in Background.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);  //checks if App is closed and let the service still running.
        if (intent != null && intent.getExtras() != null) {
            if (player != null)
                player.stop();
            //Extracting data from Intent.
            Bundle bundle = intent.getExtras();
            songPath = bundle.getStringArrayList("SongList");
            position = bundle.getInt("position");
            songName = bundle.getStringArrayList("SongName");
            albumArt = bundle.getString("albumart");

//            songart = bundle.getStringArrayList("songart");
            Uri sing = Uri.parse((String) songPath.get(position)); //Converting String path into Uri.
            player = MediaPlayer.create(this, sing);
            player.start();  //playing Song Using MediaPlayer.
            createNotification(getApplicationContext(), (String) songName.get(position), albumArt);  //shows Notification each time new song is played.
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
                    createNotification(getApplicationContext(), (String) songName.get(position),  albumArt);  //shows Notification each time new song is played.
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

    public static final int NOTIFICATION_ID_CUSTOM_BIG = 9;

    public void createNotification(Context context, String name, String art) {
        RemoteViews expandedView = new RemoteViews(context.getPackageName(), big_notification);
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pause = PendingIntent.getActivity(context, 0, notifyIntent, 0);
        PendingIntent close = PendingIntent.getActivity(context, 0, notifyIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationCompat.setContentIntent(pendingIntent);
        notificationCompat.setSmallIcon(R.drawable.ic_action_play);
        notificationCompat.setAutoCancel(false);
        notificationCompat.setCustomBigContentView(expandedView);
        notificationCompat.setContentTitle("Music Player");
        notificationCompat.setContentText("Control Audio");
        notificationCompat.getBigContentView().setTextViewText(R.id.textSongName, name);
        notificationCompat.addAction(R.id.btnPause, "Pause", pause);
        notificationCompat.addAction(R.id.btnDelete, "close", close);
        notificationCompat.setOngoing(true);

//        notificationCompat.build().flags |= Notification.FLAG_NO_CLEAR |Notification.FLAG_ONGOING_EVENT;
//          Button close=(Button)expandedView.findViewById(R.id.btnDelete);
//        setListeners(expandedView, context);


        if (art != null && !art.equals("")) {
            byte[] imag = Base64.decode(art, Base64.DEFAULT);
            try {
                Bitmap bmp = BitmapFactory.decodeByteArray(imag, 0, imag.length);
                notificationCompat.getBigContentView().setImageViewBitmap(R.id.albumart, bmp);
//                albumart.setImageBitmap(Bitmap.createScaledBitmap(bmp, width,
//                        height, false));
            } catch (Exception e) {
                e.printStackTrace();
//                notificationCompat.getBigContentView().getLayoutParams().height = 120;
//                image.getLayoutParams().width = 120;
                notificationCompat.getBigContentView().setImageViewResource(R.id.albumart, R.drawable.album_art);
                Log.e("Exception ", e.toString());
            }
        } else {
            notificationCompat.getBigContentView().setImageViewResource(R.id.albumart, R.drawable.album_art);
        }
        notificationManager.notify(NOTIFICATION_ID_CUSTOM_BIG, notificationCompat.build());


    }
}
