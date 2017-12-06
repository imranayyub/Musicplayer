package com.example.im.music;

/**
 * Created by Im on 06-12-2017.
 */

//Model Class to Get and Set data for DetailDialogFragment.

public class SongDetails {
    static String name;
    static String artist;
    static String duration;
    static String album;
    static String genre;
    static String bitrate;
    static String path;
    static String albumArt;
    static String title;

    public static void setTitle(String title) {
        SongDetails.title = title;
    }

    public static void setName(String name) {
        SongDetails.name = name;
    }

    public static String getName() {
        return name;
    }

    public static String getArtist() {
        return artist;
    }

    public static String getDuration() {
        return duration;
    }

    public static String getAlbum() {
        return album;
    }

    public static String getGenre() {
        return genre;
    }

    public static String getBitrate() {
        return bitrate;
    }

    public static void setArtist(String artist) {
        SongDetails.artist = artist;
    }

    public static void setDuration(String duration) {
        SongDetails.duration = duration;
    }

    public static void setAlbum(String album) {
        SongDetails.album = album;
    }

    public static void setGenre(String genre) {
        SongDetails.genre = genre;
    }

    public static void setBitrate(String bitrate) {
        SongDetails.bitrate = bitrate;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        SongDetails.path = path;
    }

    public static String getAlbumArt() {
        return albumArt;
    }

    public static void setAlbumArt(String albumArt) {
        SongDetails.albumArt = albumArt;
    }

    public static String getTitle() {
        return title;
    }
}
