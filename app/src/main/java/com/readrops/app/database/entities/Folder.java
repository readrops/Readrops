package com.readrops.app.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity
public class Folder implements Parcelable,  Comparable<Folder> {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    public Folder() {

    }

    @Ignore
    public Folder(String name) {
        this.name = name;
    }

    protected Folder(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    @Override
    public int compareTo(Folder o) {
        return this.getName().compareTo(o.getName());
    }
}
