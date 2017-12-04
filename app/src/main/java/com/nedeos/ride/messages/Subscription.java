package com.nedeos.ride.messages;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Karsten on 20.09.2017.
 */

public class Subscription implements Parcelable {

    private String prod;
    private String head;
    private String text;
    private String icon;
    private String shot;
    private String cost;
    private String used;

    private int stat;
    private int time;
    private int usid;

    public Subscription() {
        super();
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
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

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getUsid() {
        return usid;
    }

    public void setUsid(int usid) {
        this.usid = usid;
    }

    private Subscription(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.prod = in.readString();
        this.head = in.readString();
        this.text = in.readString();
        this.icon = in.readString();
        this.shot = in.readString();
        this.cost = in.readString();
        this.used = in.readString();

        this.stat = in.readInt();
        this.time = in.readInt();
        this.usid = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Subscription> CREATOR = new Creator<Subscription>() {
        public Subscription createFromParcel(Parcel in) {
            return new Subscription(in);
        }

        public Subscription[] newArray(int size) {
            return new Subscription[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(prod);
        parcel.writeString(head);
        parcel.writeString(text);
        parcel.writeString(icon);
        parcel.writeString(shot);
        parcel.writeString(cost);
        parcel.writeString(used);

        parcel.writeInt(stat);
        parcel.writeInt(time);
        parcel.writeInt(usid);
    }

}
