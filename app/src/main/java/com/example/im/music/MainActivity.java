package com.example.im.music;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.Button;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    int id = 0;

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

        final ArrayList<String> songs = new ArrayList<>();
        final ArrayList<String> songPath = new ArrayList<>();
        final ArrayList<String> songArt = new ArrayList<>();
        int i = 0;
        List<SongDetails> songDetailses = SQLite.select().
                from(SongDetails.class).
                queryList();
        if (songDetailses.size() == 0) {
            //Executing task in background(Updating Music Library).
            (new MyTask()).execute();
        } else {
            for (SongDetails s : songDetailses) {
                String fileName = s.getName();
                if (fileName == null)
                    fileName = s.getTitle();
                String filePath = s.getPath();
                String songArts = s.getAlbumArt();
                //here you will get list of file name and file path that present in your device
                Log.i("file details ", " name =" + fileName + " path = " + filePath);
                songs.add(i, fileName);
                songPath.add(i, filePath);
                songArt.add(i, songArts);
                i++;
            }
            CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
            registerForContextMenu(playList);
//   Checks If item on ListView is Clicked And performs Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                    play.setText("Pause");
                    Activated = 1;

                    //Intent to Start Service(Service to play Music in Background).
                    Intent intent = new Intent(MainActivity.this, MyService.class);
//                    intent.putStringArrayListExtra("SongList", songPath);
//                    intent.putStringArrayListExtra("SongName", songs);
                    bundle.putString("songname", name);
                    bundle.putInt("position",position);
                    intent.putExtras(bundle);
                    startService(intent);

                }
            });


            playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    setname(name);
                    return false;
                }
            });

        }

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
        }
        else {
//            super.onCreate(bundle);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //TODO: Reset your views
                    return false;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String search) {
                    return false; //do the default
                }

                @Override
                public boolean onQueryTextChange(final String search) {
                    //NOTE: doing anything here is optional, onNewIntent is the important bit
                    if (search.length() > 1) { //2 chars or more

                        final ArrayList<String> songs = new ArrayList<>();
                        final ArrayList<String> songPath = new ArrayList<>();
                        final ArrayList<String> songArt = new ArrayList<>();
                        int i = 0;
                        List<SongDetails> songDetailses = SQLite.select().
                                from(SongDetails.class)
                                .where(SongDetails_Table.name.like("%" + search + "%"))
                                .queryList();
                        if (songDetailses.size() == 0) {
                            //Executing task in background(Updating Music Library).
//                            (new MyTask()).execute();
                        } else {
                            for (SongDetails s : songDetailses) {
                                String fileName = s.getName();
                                if (fileName == null)
                                    fileName = s.getTitle();
                                String filePath = s.getPath();
                                String songArts = s.getAlbumArt();
                                //here you will get list of file name and file path that present in your device
                                Log.i("file details ", " name =" + fileName + " path = " + filePath);
                                songs.add(i, fileName);
                                songPath.add(i, filePath);
                                songArt.add(i, songArts);
                                i++;
                            }
                        }
                        CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

                        // Assign adapter to ListView
                        playList.setAdapter(adapter);
                        registerForContextMenu(playList);
//   Checks If item on ListView is Clicked And performs Required Function.
                        playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                String name = (String) adapterView.getItemAtPosition(position);
                                Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                                play.setText("Pause");
                                Activated = 1;

                                //Intent to Start Service(Service to play Music in Background).
                                Intent intent = new Intent(MainActivity.this, MyService.class);
//                                intent.putStringArrayListExtra("SongList", songPath);
//                                intent.putStringArrayListExtra("SongName", songs);
                                bundle.putInt("position", position);
                                bundle.putString("songname", search);
                                bundle.putInt("search", 1);
                                intent.putExtras(bundle);
                                startService(intent);

                            }
                        });


                        playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                                String name = (String) adapterView.getItemAtPosition(position);
                                setname(name);
                                return false;
                            }
                        });

                        Toast.makeText(getApplicationContext(), "Result ", Toast.LENGTH_SHORT).show();
                        //TODO: filter/return results
                    } else if (search.length() == 0) {

                        final ArrayList<String> songs = new ArrayList<>();
                        final ArrayList<String> songPath = new ArrayList<>();
                        final ArrayList<String> songArt = new ArrayList<>();
                        int i = 0;
                        List<SongDetails> songDetailses = SQLite.select().
                                from(SongDetails.class).
                                queryList();
                        if (songDetailses.size() == 0) {
                            //Executing task in background(Updating Music Library).
//                            (new MyTask()).execute();
                        } else {
                            for (SongDetails s : songDetailses) {
                                String fileName = s.getName();
                                if (fileName == null)
                                    fileName = s.getTitle();
                                String filePath = s.getPath();
                                String songArts = s.getAlbumArt();
                                //here you will get list of file name and file path that present in your device
                                Log.i("file details ", " name =" + fileName + " path = " + filePath);
                                songs.add(i, fileName);
                                songPath.add(i, filePath);
                                songArt.add(i, songArts);
                                i++;
                            }
                            CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

                            // Assign adapter to ListView
                            playList.setAdapter(adapter);
                            registerForContextMenu(playList);
//   Checks If item on ListView is Clicked And performs Required Function.
                            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                                    String name = (String) adapterView.getItemAtPosition(position);
                                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                                    play.setText("Pause");
                                    Activated = 1;

                                    //Intent to Start Service(Service to play Music in Background).
                                    Intent intent = new Intent(MainActivity.this, MyService.class);
//                                    intent.putStringArrayListExtra("SongList", songPath);
//                                    intent.putStringArrayListExtra("SongName", songs);
                                    bundle.putInt("position", position);
                                    intent.putExtras(bundle);
                                    bundle.putInt("search", 0);
                                    startService(intent);

                                }
                            });


                            playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                                    String name = (String) adapterView.getItemAtPosition(position);
                                    setname(name);
                                    return false;
                                }
                            });

                        }
                        //TODO: reset the displayed data
                    }
                    return false;
                }

            });

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_playList) {
            // Handle the PLayList


            final ArrayList<String> songs = new ArrayList<>();
            final ArrayList<String> songPath = new ArrayList<>();
            final ArrayList<String> songArt = new ArrayList<>();
            int i = 0;
            List<PlayList> playLists = SQLite.select().
                    from(PlayList.class)
//                    .where(SongDetails_Table.name.like("%" + search + "%"))
                    .queryList();
            if (playLists.size() == 0) {
                //Executing task in background(Updating Music Library).
//                            (new MyTask()).execute();
            } else {
                for (PlayList s : playLists) {
                    String fileName = s.getName();
                    if (fileName == null)
                        fileName = s.getTitle();
                    String filePath = s.getPath();
                    String songArts = s.getAlbumArt();
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    songPath.add(i, filePath);
                    songArt.add(i, songArts);
                    i++;
                }
            }
            if(songArt.size()==0)
            {
                Toast.makeText(getApplicationContext(),"Add Songs to Playlist..",Toast.LENGTH_SHORT).show();
            }
            CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
            registerForContextMenu(playList);
//   Checks If item on ListView is Clicked And performs Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                    play.setText("Pause");
                    Activated = 1;

                    //Intent to Start Service(Service to play Music in Background).
                    Intent intent = new Intent(MainActivity.this, MyService.class);
//                                intent.putStringArrayListExtra("SongList", songPath);
//                                intent.putStringArrayListExtra("SongName", songs);
                    bundle.putInt("position", position);
//                    bundle.putString("songname", );
                    bundle.putInt("search", 2);
                    intent.putExtras(bundle);
                    startService(intent);

                }
            });


            playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    setname(name);
                    return false;
                }
            });


        } else if (id == R.id.home) {
//            onBackPressed();
//            onRestart();
            final ArrayList<String> songs = new ArrayList<>();
            final ArrayList<String> songPath = new ArrayList<>();
            final ArrayList<String> songArt = new ArrayList<>();
            int i = 0;
            List<SongDetails> songDetailses = SQLite.select().
                    from(SongDetails.class).
                    queryList();
            if (songDetailses.size() == 0) {
                //Executing task in background(Updating Music Library).
                (new MyTask()).execute();
            } else {
                for (SongDetails s : songDetailses) {
                    String fileName = s.getName();
                    if (fileName == null)
                        fileName = s.getTitle();
                    String filePath = s.getPath();
                    String songArts = s.getAlbumArt();
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    songPath.add(i, filePath);
                    songArt.add(i, songArts);
                    i++;
                }
                CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

                // Assign adapter to ListView
                playList.setAdapter(adapter);
                registerForContextMenu(playList);
//   Checks If item on ListView is Clicked And performs Required Function.
                playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String name = (String) adapterView.getItemAtPosition(position);
                        Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                        play.setText("Pause");
                        Activated = 1;

                        //Intent to Start Service(Service to play Music in Background).
                        Intent intent = new Intent(MainActivity.this, MyService.class);
//                    intent.putStringArrayListExtra("SongList", songPath);
//                    intent.putStringArrayListExtra("SongName", songs);
                        bundle.putString("songname", name);
                        bundle.putInt("position",position);
                        bundle.putInt("search",0);
                        intent.putExtras(bundle);
                        startService(intent);

                    }
                });


                playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String name = (String) adapterView.getItemAtPosition(position);
                        setname(name);
                        return false;
                    }
                });

            }


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
            final ArrayList<String> songArt = new ArrayList<>();

            if (songList != null) {
                for (int i = 0; i < songList.size() / 2; i++) {
                    String fileName = songList.get(i).get("songtitle");
                    if (fileName == null)
                        fileName = songList.get(i).get("file_name");
                    String filePath = songList.get(i).get("file_path");
                    String songArts = songList.get(i).get("albumart");
                    //here you will get list of file name and file path that present in your device
                    Log.i("file details ", " name =" + fileName + " path = " + filePath);
                    songs.add(i, fileName);
                    songPath.add(i, filePath);
                    songArt.add(i, songArts);

//                    long songCount = SQLite.select().from(SongDetails.class).count();//new Select().from(SongDetails.class).count();
//                    Log.d("NO OF ROWS :", String.valueOf(songCount));
                    SongDetails songDetails = new SongDetails();
//                    songDetails.delete();
                    songDetails.setId(id);
                    id++;
                    songDetails.setName(fileName);
                    songDetails.setBitrate(songList.get(i).get("bitrate"));
                    songDetails.setDuration(songList.get(i).get("duration"));
                    songDetails.setAlbum(songList.get(i).get("album"));
                    songDetails.setGenre(songList.get(i).get("genre"));
                    songDetails.setArtist(songList.get(i).get("artist"));
                    songDetails.setPath(songList.get(i).get("file_path"));
                    songDetails.setAlbumArt(songList.get(i).get("albumart"));
                    songDetails.setTitle(songList.get(i).get("songtitle"));
//                    songDetails.insert();
                    songDetails.save();


                }
            }

            //Setting Adapter  on ListView.
            CustomAdapterforList adapter = new CustomAdapterforList(MainActivity.this, songArt, songs);

            // Assign adapter to ListView
            playList.setAdapter(adapter);
            registerForContextMenu(playList);

//   Checks If item on ListView is Clicked And performs Required Function.
            playList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "Playing : " + name, Toast.LENGTH_SHORT).show();

                    play.setText("Pause");
                    Activated = 1;

                    //Intent to Start Service(Service to play Music in Background).
                    Intent intent = new Intent(MainActivity.this, MyService.class);
//                    intent.putStringArrayListExtra("SongList", songPath);
//                    intent.putStringArrayListExtra("SongName", songs);
                    bundle.putInt("position", position);
                    bundle.putInt("search", 0);
                    intent.putExtras(bundle);
                    startService(intent);

                }
            });


            playList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = (String) adapterView.getItemAtPosition(position);
                    setname(name);
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
                        if (image != null) {
                            albumArt = Base64.encodeToString(image, Base64.DEFAULT);
                        }
                        Log.d("log", "bitrate" + image);
                        song.put("albumart", albumArt);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Options: ");
        menu.add(0, v.getId(), 0, "Add to Playlist");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "Details");
        menu.add(0, v.getId(), 0, "Cancel");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Add to Playlist") {
            Toast.makeText(getApplicationContext(), "Adding to Playlist..", Toast.LENGTH_LONG).show();
            PlayList playList = new PlayList();
            String songname = getSongDetailName();
            List<SongDetails> songDetailses = SQLite.select().
                    from(SongDetails.class).where(SongDetails_Table.name.like(songname)).
                    queryList();
//        int size=songDetailses.size();
            SongDetails song = songDetailses.get(0);
            playList.setId(song.getId());
            playList.setName(song.getName());
            playList.setAlbum(song.getAlbum());
            playList.setAlbumArt(song.getAlbumArt());
            playList.setBitrate(song.getBitrate());
            playList.setGenre(song.getGenre());
            playList.setPath(song.getPath());
            playList.setTitle(song.getTitle());
            playList.save();
        } else if (item.getTitle() == "Details") {
            Toast.makeText(getApplicationContext(), "Song Details..", Toast.LENGTH_LONG).show();


            dialog.show(manager, "YourDialog");
        } else if (item.getTitle() == "Cancel") {

        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.stop: {
                stopService(new Intent(MainActivity.this, MyService.class));
                play.setText("play");
                Activated = 1;
                break;
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


    @Override
    protected void onDestroy() {
        if (!isMyServiceRunning(MyService.class)) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    static String songDetailName;

    public void setname(String name) {
        this.songDetailName = name;
    }

    public static String getSongDetailName() {
        return songDetailName;
    }

}