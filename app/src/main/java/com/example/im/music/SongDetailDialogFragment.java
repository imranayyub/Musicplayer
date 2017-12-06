package com.example.im.music;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


/**
 * Created by Im on 06-12-2017.
 */

public class SongDetailDialogFragment extends DialogFragment implements View.OnClickListener {
    TextView name1;
    Button back;
    long hours = 0;
    String time, artist, album, genre, name, albumart;

    ImageView image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflating the dialogfragment layout
        View rootView = inflater.inflate(R.layout.songdetail_dialog, container, false);
        name1 = (TextView) rootView.findViewById(R.id.name1);
//        image=(ImageView)rootView.findViewById(R.id.image);
        back = (Button) rootView.findViewById(R.id.back);
        back.setOnClickListener(this); //setting OnClickListener on Back Button.
//        Uri uri = Uri.parse(SongDetails.getAlbumArt());
//        Glide.with(getActivity()).load(uri)
//                .thumbnail(0.5f)
//                .crossFade()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(image);
        //Converting Duration in Proper time Format.
        long duration = Long.parseLong(SongDetails.getDuration());
        hours = (duration / 3600000);
        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
        String minutes = String.valueOf(remaining_minutes);
        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000));
        String seconds = String.valueOf(remaining_seconds);
        seconds = seconds.substring(0, 2);
        if (Integer.parseInt(seconds) > 60)
            seconds = "0" + seconds.substring(0, 1);
        if (hours > 0) {
            time = hours + ":" + minutes + ":" + seconds;
        } else {
            time = minutes + ":" + seconds;
        }
        //Converting Bitrate into proper Format
        String bitrate = SongDetails.getBitrate();
        String[] separated = bitrate.split("0");
        bitrate = separated[0];
        if (bitrate.equals("32"))
            bitrate = "320";
        getDialog().setTitle("Song Details :"); //Sets title of Dialog Fragment.
        getDialog().setCancelable(false);

        artist = SongDetails.getArtist();
        if (artist == null)
            artist = "--";
        album = SongDetails.getAlbum();
        if (album == null)
            album = "--";
        genre = SongDetails.getGenre();
        if (genre == null)
            genre = "--";
        name = SongDetails.getTitle();
        if (name == null)
            name = SongDetails.getName();
        //Displaying data in TextView of DialogFragment.
        name1.setText("Name : " + name
                + "\nPath : " + SongDetails.getPath()
                + "\nArtist : " + artist
                + "\nAlbum : " + album
                + "\nDuration : " + time
                + "\nGenre : " + genre
                + "\nBitrate : " + bitrate + " kbps");

//        albumart = SongDetails.getAlbumArt();
//        if (albumart != null) {
//            byte[] imag = albumart.getBytes();
//            Bitmap bmp = BitmapFactory.decodeByteArray(imag, 0, imag.length);
//
//            image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
//                    image.getHeight(), false));
//        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.back:
                dismiss(); //Dismisses the Dialog Fragment.
                break;
        }
    }
}
