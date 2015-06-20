package com.example.android.spotifystreamer.app;

import android.os.Parcel;
import android.os.Parcelable;


public class ArtistResult implements Parcelable{
    private String id;
    private String name;
    private String albumUrl;

    protected ArtistResult(Parcel in) {
        id = in.readString();
        name = in.readString();
        albumUrl = in.readString();
    }

    public ArtistResult(String id, String name, String imageUrl) {
        this.id = id;
        this.name=name;
        this.albumUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(albumUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ArtistResult> CREATOR = new Parcelable.Creator<ArtistResult>() {
        @Override
        public ArtistResult createFromParcel(Parcel in) {
            return new ArtistResult(in);
        }

        @Override
        public ArtistResult[] newArray(int size) {
            return new ArtistResult[size];
        }
    };
}
