package com.nedeos.ride.users;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Karsten on 20.09.2017.
 */

public class User implements Parcelable {

    private int usid;

    private String head;
    private String icon;
    private String shot;

    private String image_1;
    private String image_2;
    private String image_3;

    private int stat;

    public User() {
        super();
    }

    public int getUsid() {
        return usid;
    }

    public void setUsid(int usid) {
        this.usid = usid;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
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

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public String getImage_1() {
        return image_1;
    }

    public void setImage_1(String image_1) {
        this.image_1 = image_1;
    }

    public String getImage_2() {
        return image_2;
    }

    public void setImage_2(String image_2) {
        this.image_2 = image_2;
    }

    public String getImage_3() {
        return image_3;
    }

    public void setImage_3(String image_3) {
        this.image_3 = image_3;
    }

    private User(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.usid = in.readInt();

        this.head = in.readString();
        this.icon = in.readString();
        this.shot = in.readString();

        this.stat = in.readInt();

        this.image_1 = in.readString();
        this.image_2 = in.readString();
        this.image_3 = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(usid);

        parcel.writeString(head);
        parcel.writeString(icon);
        parcel.writeString(shot);

        parcel.writeInt(stat);

        parcel.writeString(image_1);
        parcel.writeString(image_2);
        parcel.writeString(image_3);
    }

}
