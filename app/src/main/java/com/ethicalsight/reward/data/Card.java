package com.ethicalsight.reward.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Card implements Parcelable {

    public static final String ID = "id";
    public static final String BRAND = "brand";
    public static final String NUMBER = "number";


    public static final Creator<Card> CREATOR
            = new Creator<Card>() {
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    private long id;
    private Brand brand;
    private String number;

    public Card() {

    }

    private Card(Parcel in) {
        id = in.readLong();
        brand = in.readParcelable(Brand.class.getClassLoader());
        number = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeParcelable(brand, flags);
        out.writeString(number);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
