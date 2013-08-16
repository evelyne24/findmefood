package org.codeandmagic.findmefood.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

/**
 * Created by evelyne24.
 */
public class OpeningHours implements Parcelable {

    @SerializedName("open_now")
    private boolean openNow;

    public OpeningHours() {
        this.openNow = false;
    }

    public OpeningHours(Parcel in) {
        openNow = in.readInt() > 0;
    }

    public OpeningHours(boolean openNow) {
        this.openNow = openNow;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || OpeningHours.class != o.getClass()) return false;

        OpeningHours that = (OpeningHours) o;

        if (openNow != that.openNow) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (openNow ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(openNow ? 1 : 0);
    }

    public static final Parcelable.Creator<OpeningHours> CREATOR
            = new Parcelable.Creator<OpeningHours>() {
        public OpeningHours createFromParcel(Parcel in) {
            return new OpeningHours(in);
        }

        public OpeningHours[] newArray(int size) {
            return new OpeningHours[size];
        }
    };
}
