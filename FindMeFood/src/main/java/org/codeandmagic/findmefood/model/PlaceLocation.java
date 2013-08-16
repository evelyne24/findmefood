package org.codeandmagic.findmefood.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import static org.codeandmagic.findmefood.Consts.UNSET;

/**
 * Created by evelyne24.
 */
public class PlaceLocation implements Parcelable {

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    public PlaceLocation() {
        this.latitude = UNSET;
        this.longitude = UNSET;
    }

    public PlaceLocation(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public PlaceLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || PlaceLocation.class != o.getClass()) return false;

        PlaceLocation that = (PlaceLocation) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Parcelable.Creator<PlaceLocation> CREATOR
            = new Parcelable.Creator<PlaceLocation>() {

        public PlaceLocation createFromParcel(Parcel in) {
            return new PlaceLocation(in);
        }

        public PlaceLocation[] newArray(int size) {
            return new PlaceLocation[size];
        }
    };
}
