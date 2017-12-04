/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nedeos.ride.purchases;

import android.os.Parcel;
import android.os.Parcelable;

import com.nedeos.ride.RideApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase implements Parcelable {

    String mItemType;

    private String mOrderId;
    private String mPackageName;
    private String mSku;

    private long mPurchaseTime;
    private int mPurchaseState;

    private String mDeveloperPayload;
    private String mToken;
    private String mOriginalJson;
    private String mSignature;

    public Purchase(String itemType, String jsonPurchaseInfo, String signature) throws JSONException {
        mItemType = itemType;

        mOriginalJson = jsonPurchaseInfo;
        JSONObject o = new JSONObject(mOriginalJson);

        mOrderId = o.optString(RideApplication.ARG_ORDER_ID);
        mPackageName = o.optString(RideApplication.ARG_PACKAGE_NAME);
        mSku = o.optString(RideApplication.ARG_PRODUCT_ID);
        mPurchaseTime = o.optLong(RideApplication.ARG_PURCHASE_TIME);
        mPurchaseState = o.optInt(RideApplication.ARG_PURCHASE_STATE);
        mDeveloperPayload = o.optString(RideApplication.ARG_DEVELOPER_PAYLOAD);
        mToken = o.optString(RideApplication.ARG_TOKEN, o.optString(RideApplication.ARG_PURCHASE_TOKEN));

        mSignature = signature;
    }

    public Purchase() {
        super();
    }

    public String getItemType() {
        return mItemType;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getSku() {
        return mSku;
    }

    public long getPurchaseTime() {
        return mPurchaseTime;
    }

    public int getPurchaseState() {
        return mPurchaseState;
    }

    public String getDeveloperPayload() {
        return mDeveloperPayload;
    }

    public String getToken() {
        return mToken;
    }

    public String getOriginalJson() {
        return mOriginalJson;
    }

    public String getSignature() {
        return mSignature;
    }

    @Override
    public String toString() {
        return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson;
    }

    private Purchase(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        this.mItemType = in.readString();
        this.mOrderId = in.readString();
        this.mPackageName = in.readString();
        this.mSku = in.readString();
        this.mPurchaseTime = in.readLong();
        this.mPurchaseState = in.readInt();
        this.mDeveloperPayload = in.readString();
        this.mToken = in.readString();
        this.mOriginalJson = in.readString();
        this.mSignature = in.readString();
    }

    public static final Parcelable.Creator<Purchase> CREATOR = new Parcelable.Creator<Purchase>() {
        public Purchase createFromParcel(Parcel in) {
            return new Purchase(in);
        }

        public Purchase[] newArray(int size) {
            return new Purchase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mItemType);
        parcel.writeString(mOrderId);
        parcel.writeString(mPackageName);
        parcel.writeString(mSku);
        parcel.writeLong(mPurchaseTime);
        parcel.writeInt(mPurchaseState);
        parcel.writeString(mDeveloperPayload);
        parcel.writeString(mToken);
        parcel.writeString(mOriginalJson);
        parcel.writeString(mSignature);
    }
}
