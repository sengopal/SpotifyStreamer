package com.example.android.spotifystreamer.app;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by sengopal on 7/30/15.
 */
public class PlayTrack implements Parcelable{
    private String artist;
    private String album;
    private String albumImg;
    private String track;
    private String previewUrl;

    protected PlayTrack(Parcel in) {
        artist = in.readString();
        album = in.readString();
        albumImg = in.readString();
        track = in.readString();
        previewUrl = in.readString();
    }

    public PlayTrack(String artist, String album, String albumImg, String track, String previewUrl) {
        this.artist = artist;
        this.album = album;
        this.albumImg = albumImg;
        this.track = track;
        this.previewUrl = previewUrl;
    }

    public PlayTrack(Track track) {
        this.artist = track.artists.get(0).name;
        this.album = track.album.name;
        this.albumImg = extractImg(track.album);
        this.track = track.name;
        this.previewUrl = track.preview_url;
    }

    private String extractImg(AlbumSimple album) {
        if(null!=album.images && !album.images.isEmpty() && null!=album.images.get(0) && null!=album.images.get(0).url) {
            return album.images.get(0).url;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumImg() {
        return albumImg;
    }

    public void setAlbumImg(String albumImg) {
        this.albumImg = albumImg;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(albumImg);
        dest.writeString(track);
        dest.writeString(previewUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PlayTrack> CREATOR = new Parcelable.Creator<PlayTrack>() {
        @Override
        public PlayTrack createFromParcel(Parcel in) {
            return new PlayTrack(in);
        }

        @Override
        public PlayTrack[] newArray(int size) {
            return new PlayTrack[size];
        }
    };
}
