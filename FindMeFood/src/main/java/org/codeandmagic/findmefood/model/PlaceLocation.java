package org.codeandmagic.findmefood.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by evelyne24.
 */
public class PlaceLocation {

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

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
}
