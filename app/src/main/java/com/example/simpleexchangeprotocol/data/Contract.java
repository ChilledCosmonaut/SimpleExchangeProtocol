package com.example.simpleexchangeprotocol.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Contract implements Parcelable {
    public Integer Number;
    public Name Creator;
    public Name Partner;
    public ArrayList<String> Images = new ArrayList<>();

    // Default Constructor
    public Contract() {}

    // Constructor for Parcel (Reads data in the exact order it was written)
    protected Contract(Parcel in) {
        // Read Integer (handle null safety)
        Number = (Integer) in.readValue(Integer.class.getClassLoader());

        // Read Name objects (handle null safety)
        Creator = in.readParcelable(Name.class.getClassLoader());
        Partner = in.readParcelable(Name.class.getClassLoader());

        // Read ArrayList of Strings
        Images = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write Integer (handle null safety)
        dest.writeValue(Number);

        // Write Name objects
        dest.writeParcelable((Parcelable) Creator, flags);
        dest.writeParcelable((Parcelable) Partner, flags);

        // Write ArrayList of Strings
        dest.writeStringList(Images);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // The CREATOR is mandatory. It tells Android how to reconstruct the object.
    public static final Creator<Contract> CREATOR = new Creator<Contract>() {
        @Override
        public Contract createFromParcel(Parcel in) {
            return new Contract(in);
        }

        @Override
        public Contract[] newArray(int size) {
            return new Contract[size];
        }
    };
}