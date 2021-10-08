package com.ethicalsight.reward.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Account implements Parcelable {

    public static final String COLLECTION = "ACCOUNTS";
    public static final String DOCUMENT = "account";
    public static final String ID = "id";
    public static final String CARDS = "cards";
    public static final String RECEIPTS = "receipts";

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
    private String id;
    private List<Card> cards;

    public Account() {

    }

    protected Account(Parcel in) {
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
