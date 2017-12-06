package com.example.im.music;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.Button;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.im.music.R.layout.big_notification;
import static com.example.im.music.R.layout.design_layout_tab_icon;
import static com.example.im.music.R.layout.listview;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    //Defining Variables.
    Button play, stop;
    ListView playList;
    ProgressDialog progressDialog;
    ArrayList<HashMap<String, String>> songList;
    int Activated = 1;
    Bundle bundle = new Bundle();
    FragmentManager manager = getFragmentManager();
    SongDetailDialogFragment dialog = new SongDetailDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playList = (ListView) findViewById(R.id.playList);
        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);

        //Executing task in background(Updating Music Library).
        (new MyTask()).execute();


        //Floationg Action Bar(Updating Music Library).
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Updating Music Library", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                fileList.clear();
                (new MyTask()).execute();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Function to preform Action in case Back button is pressed/
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //AsyncTask
    class MyTask extends AsyncTask<Integer, Integer, String> {
        //to do task in Background.
        @Override
        protected String doInBackground(Integer... params) {
            songList = getPlayList("/storage/");
            return "1";
        }

        //When the task is background is done(In this case updating Music Library).
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final ArrayList<String> songs = new ArrayList<>();
            final ArrayList<String> songPath = new ArrayList<>();

            if (songList != null) {
                for (int i = 0; i < songList.size() / 2; i++) {
                    String fileName = songList.get(i).get("songtitle");
                    if(fileName==null)
                        fileName=songList.get(i).get("file_name");
                    String filePath = songList.get(i).get("file_path");
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    songPath.add(i, filePath);
                }
            }
            //Setting Adapter  on ListView.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    listview, R.id.name, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
//            Checks If item on ListView is Clicked And performs Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                    //Showing Custom Notification with Control Button
                    createNotification(getApplicationContext(), name);

                    play.setText("Pause");
                    Activated = 1;

                    //Intent to Start Service(Service to play Music in Background).
                    Intent intent = new Intent(MainActivity.this, MyService.class);

                    intent.putStringArrayListExtra("SongList", songPath);
                    intent.putStringArrayListExtra("SongName", songs);
                    bundle.putInt("position", position);
                    intent.putExtras(bundle);
                    startService(intent);

                }
            });
            playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    SongDetails.setName(name);
                    SongDetails.setBitrate(songList.get(position).get("bitrate"));
                    SongDetails.setDuration(songList.get(position).get("duration"));
                    SongDetails.setAlbum(songList.get(position).get("album"));
                    SongDetails.setGenre(songList.get(position).get("genre"));
                    SongDetails.setArtist(songList.get(position).get("artist"));
                    SongDetails.setPath(songList.get(position).get("file_path"));
                    SongDetails.setAlbumArt(songList.get(position).get("albumart"));
                    SongDetails.setTitle(songList.get(position).get("songtitle"));
                    Toast.makeText(getApplicationContext(), "Long Pressed : " + name, Toast.LENGTH_SHORT).show();

                    dialog.show(manager, "YourDialog");

                    return false;
                }
            });
        }

        //Before the task(Music Library is updated)is executed.
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Updating Music Library...", true);
        }
    }

    //Function to get All the Mp3 files from directory.
    ArrayList<HashMap<String, String>> fileList = new ArrayList<>();
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    ArrayList<HashMap<String, String>> getPlayList(String rootPath) {
        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.isDirectory()) {
                        getPlayList(file.getAbsolutePath());
                    }
                } else {
                    if (file.getName().endsWith(".mp3")) {
                        HashMap<String, String> song = new HashMap<>();
                        song.put("file_path", file.getAbsolutePath());
                        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
                        song.put("file_name", file.getName());
                        song.put("songtitle", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                        song.put("artist", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
                        song.put("duration", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        song.put("album", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                        song.put("genre", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
                        song.put("bitrate", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
                        byte[] image = mediaMetadataRetriever.getEmbeddedPicture();

                        String albumArt = "";
                        Uri uri = null;
                        if (image != null) {
                            albumArt = Base64.encodeToString(image, Base64.DEFAULT);
                            uri = Uri.parse(albumArt);
                        }
                        Log.d("log", "bitrate" + mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//                        Log.d("Image : ", uri);
                        song.put("albumart", String.valueOf(image));
//                        System.out.println(uri);
                        fileList.add(song);
                    }
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.stop: {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
            case R.id.play: {

                if (Activated == 1) {
//                    mp.pause();
                    MyService.pause();
                    Activated = 0;
                    play.setText("Play");
                } else {
//                    mp.start();
                    MyService.play();
                    play.setText("Pause");
                    Activated = 1;
                }
            }

        }
    }


    Button close;
    private static final int NOTIFICATION_ID_CUSTOM_BIG = 9;

    public static void createNotification(Context context, String name) {
        RemoteViews expandedView = new RemoteViews(context.getPackageName(), big_notification);
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationCompat.setContentIntent(pendingIntent);
        notificationCompat.setSmallIcon(R.drawable.ic_action_play);
        notificationCompat.setAutoCancel(false);
        notificationCompat.setCustomBigContentView(expandedView);
        notificationCompat.setContentTitle("Music Player");
        notificationCompat.setContentText("Control Audio");
        notificationCompat.getBigContentView().setTextViewText(R.id.textSongName, name);
//          close=(Button)expandedView.findViewById(R.id.btnDelete);
//        setListeners(expandedView, context);

        notificationManager.notify(NOTIFICATION_ID_CUSTOM_BIG, notificationCompat.build());


    }

}
