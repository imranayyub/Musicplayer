package com.example.im.music;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
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
import android.widget.Toast;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.im.music.R.layout.listview;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    Button play;
    ListView playList;
    MediaPlayer mp;
    ProgressDialog progressDialog;
    ArrayList<HashMap<String, String>> songList;
    int Activated = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        playList = (ListView) findViewById(R.id.playList);
        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(this);
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
            final ArrayList<Uri> path = new ArrayList<>();
            final ArrayList<String> sang = new ArrayList<>();
            if (songList != null) {
                for (int i = 0; i < songList.size(); i++) {
                    String fileName = songList.get(i).get("file_name");
                    String filePath = songList.get(i).get("file_path");
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    path.add(i, Uri.parse(filePath));
                    sang.add(i,filePath);
                }
            }
            //Setting Adapter  on ListView.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    listview, R.id.name, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
//            Checking If item on ListView is Clicked And performing Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();
//                    Bundle bundle = new Bundle();
//                    bundle.putString("Song",path.get(position).toString());
                    //Creating Notification and displaying.
                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent nintent = PendingIntent.getActivity(getApplicationContext(), 0,
                            notificationIntent, 0);
                    Notification noti = new Notification.Builder(MainActivity.this)
                            .setContentTitle("Playing : ")
                            .setContentText(name).setSmallIcon(R.drawable.cool)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.music))
                            .setContentIntent(nintent)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // hide the notification after its selected
                    noti.flags |= Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(0, noti);
                    //Intent to Start Service(Service to play Music in Background).
                    Intent intent = new Intent(MainActivity.this, MyService.class);

//                    intent.putStringArrayListExtra("SongList",sang);
//                    intent.putExtra("position",position);
                    intent.putExtra("Song", path.get(position).toString());
                    startService(intent);
                    Activated = 1;
                    play.setText("Stop");
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
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
                    song.put("file_name", file.getName());
//                    song.put("artist", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
//                    song.put("duration", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//                    song.put("album", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//                    song.put("genre", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
//                    song.put("bitrate", mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//                    String image = mediaMetadataRetriever.getEmbeddedPicture().toString();
//                    Log.d("log", "bitrate" + mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//                    Log.d("Image : ", image);
                    fileList.add(song);
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
            case R.id.play: {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
//                if (Activated == 1) {
//                    mp.pause();
//                    Activated = 0;
//                    play.setText("Play");
//                } else {
//                    mp.start();
//                    play.setText("Pause");
//                    Activated = 1;
//                }
//            }

        }
    }

}
