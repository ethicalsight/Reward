package com.ethicalsight.reward.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class Brand implements Parcelable {

    public static final String COLLECTION = "BRANDS";
    public static final String DOCUMENT = "BRAND";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FORMAT = "format";
    public static final String BG_COLOR = "bg_color";

    public static final Creator<Brand> CREATOR
            = new Creator<Brand>() {
        public Brand createFromParcel(Parcel in) {
            return new Brand(in);
        }

        public Brand[] newArray(int size) {
            return new Brand[size];
        }
    };

    private long id;
    private String name;
    private String logo;
    private String format;
    private String bgColor;

    public Brand() {

    }

    private Brand(Parcel in) {
        id = in.readLong();
        name = in.readString();
        format = in.readString();
        bgColor = in.readString();
        logo = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeString(format);
        out.writeString(bgColor);
        out.writeString(logo);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Brand brand = (Brand) o;
        return id == brand.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
