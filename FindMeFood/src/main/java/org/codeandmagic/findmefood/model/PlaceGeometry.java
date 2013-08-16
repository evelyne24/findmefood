package org.codeandmagic.findmefood.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by evelyne24.
 */
public class PlaceGeometry implements Parcelable {

    private PlaceLocation location;

    public PlaceGeometry() {
        location = new PlaceLocation();
    }

    public PlaceGeometry(Parcel in) {
        location = in.readParcelable(PlaceLocation.class.getClassLoader());
    }

    public PlaceGeometry(PlaceLocation location) {
        this.location = location;
    }

    public PlaceLocation getLocation() {
        return location;
    }

    public void setLocation(PlaceLocation location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || PlaceGeometry.class != o.getClass()) return false;

        PlaceGeometry that = (PlaceGeometry) o;

        if (location != null ? !location.equals(that.location) : that.location != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, 0);
    }

    public static final Parcelable.Creator<PlaceGeometry> CREATOR
            = new Parcelable.Creator<PlaceGeometry>() {
        public PlaceGeometry createFromParcel(Parcel in) {
            return new PlaceGeometry(in);
        }

        public PlaceGeometry[] newArray(int size) {
            return new PlaceGeometry[size];
        }
    };
}
