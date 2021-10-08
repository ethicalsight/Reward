package com.ethicalsight.reward.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Receipt implements Parcelable {

    public static final Creator<Receipt> CREATOR
            = new Creator<Receipt>() {
        public Receipt createFromParcel(Parcel in) {
            return new Receipt(in);
        }

        public Receipt[] newArray(int size) {
            return new Receipt[size];
        }
    };

    private Brand brand;

    public Receipt() {

    }

    private Receipt(Parcel in) {
        brand = in.readParcelable(Brand.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(brand, flags);
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
