package com.nedeos.ride.messages;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Karsten on 20.09.2017.
 */

public class Message implements Parcelable {

    private String head;
    private String text;
    private String link;
    private String icon;
    private String shot;
    private String prod;

    private long time;

    private int noid;

    public Message() {
        super();
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getShot() {
        return shot;
    }

    public void setShot(String shot) {
        this.shot = shot;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getNoid() {
        return noid;
    }

    public void setNoid(int noid) {
        this.noid = noid;
    }

    private Message(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.head = in.readString();
        this.text = in.readString();
        this.link = in.readString();
        this.icon = in.readString();
        this.shot = in.readString();
        this.prod = in.readString();

        this.time = in.readLong();

        this.noid = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(head);
        parcel.writeString(text);
        parcel.writeString(link);
        parcel.writeString(icon);
        parcel.writeString(shot);
        parcel.writeString(prod);

        parcel.writeLong(time);

        parcel.writeInt(noid);
    }

}
