package com.example.im.music;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        playList = (ListView) findViewById(R.id.playList);
        play = (Button) findViewById(R.id.play);
        play.setOnClickListener(this);
        mp = MediaPlayer.create(this, R.raw.barish);

        //Executing task in background(Updating Music Library).
        (new MyTask()).execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
            songList = getPlayList("/storage/ext_sd/");
            return "1";
        }

        //When the task is background is done(In this case updating Music Library).
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            final ArrayList<String> songs = new ArrayList<>();
            final ArrayList<String> path = new ArrayList<>();
            if (songList != null) {
                for (int i = 0; i < songList.size(); i++) {
                    String fileName = songList.get(i).get("file_name");
                    String filePath = songList.get(i).get("file_path");
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    path.add(i, filePath);
                }
            }
            //Setting Adapter  on ListView.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                    listview, R.id.name, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
//            Checking If item on ListView is Clicked And performing Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String name = (String) adapterView.getItemAtPosition(i);
                    Toast.makeText(getApplicationContext(), "PLaying : " + name, Toast.LENGTH_SHORT).show();
                    Uri paths = Uri.parse(path.get(i));
                    mp.stop();
                    mp = MediaPlayer.create(MainActivity.this, paths);
                    mp.start();
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
    ArrayList<HashMap<String, String>> getPlayList(String rootPath) {
        ArrayList<HashMap<String, String>> fileList = new ArrayList<>();


        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles(); //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getPlayList(file.getAbsolutePath()) != null) {
                        fileList.addAll(getPlayList(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    HashMap<String, String> song = new HashMap<>();
                    song.put("file_path", file.getAbsolutePath());
                    song.put("file_name", file.getName());
                    fileList.add(song);
                }
            }
            return fileList;
        } catch (Exception e) {
            return null;
        }
    }
//     D
//    public void getMp3Songs() {
//
//        Uri allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
//        Cursor cursor;
//        cursor = managedQuery(allsongsuri, STAR, selection, null, null);
//
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    song_name = cursor
//                            .getString(cursor
//                                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//                    int song_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media._ID));
//
//                    String fullpath = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.DATA));
//                    fullsongpath.add(fullpath);
//
//                    album_name = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                    int album_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//
//                    artist_name = cursor.getString(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                    int artist_id = cursor.getInt(cursor
//                            .getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
//
//
//                } while (cursor.moveToNext());
//
//            }
//            cursor.close();
//            db.closeDatabase();
//        }
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.play: {
                mp.stop();
            }

        }
    }
}
