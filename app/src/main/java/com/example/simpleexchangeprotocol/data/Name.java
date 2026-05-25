package com.example.simpleexchangeprotocol.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Name implements Parcelable {
    public String firstName;
    public String lastName;

    public Name(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Constructor for Parcel
    protected Name(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Name> CREATOR = new Creator<Name>() {
        @Override
        public Name createFromParcel(Parcel in) {
            return new Name(in);
        }

        @Override
        public Name[] newArray(int size) {
            return new Name[size];
        }
    };
}