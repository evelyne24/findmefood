package org.codeandmagic.findmefood;

import android.location.Location;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.codeandmagic.findmefood.Consts.IE6;
import static org.codeandmagic.findmefood.Consts.UserSettings.DEFAULT_RADIUS;
import static org.codeandmagic.findmefood.Consts.UserSettings.FRESH_LOCATION_INTERVAL;

/**
 * Created by evelyne24.
 */
public class LocationUtils {

    public static final int INITIAL_DISPLAY_ZOOM = 15;
    public static final int MIN_DATA_FETCH_ZOOM = 17;
    public static final int CLUSTERING_ZOOM_LEVEL = 13;

    public static final double EARTH_RADIUS = 6378137;
    public static final double MIN_LATITUDE = -85.05112878;
    public static final double MAX_LATITUDE = 85.05112878;
    public static final double MIN_LONGITUDE = -180;
    public static final double MAX_LONGITUDE = 180;

    public static float getDistanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    public static boolean isLocationFresh(Location location) {
        return location != null &&
                System.currentTimeMillis() - location.getTime() < FRESH_LOCATION_INTERVAL &&
                location.getAccuracy() <= DEFAULT_RADIUS;
    }

    public static int coordinateToInt(double coordinate) {
        return (int) (coordinate * IE6);
    }

    public static double coordinateFromInt(int intCoordinate) {
        return intCoordinate / IE6;
    }

    /**
     * Clip a value to a minimum and maximum values.
     * @param n
     * @param min
     * @param max
     * @return
     */
    public static double clip(double n, double min, double max) {
        return min(max(n, min), max);
    }

    public static int clip(int n, int min, int max) {
        return min(max(n, min), max);
    }

    /**
     * Make sure latitude stays within correct bounds.
     * @param latitude
     * @return
     */
    public static double clipLatitude(double latitude) {
        return clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
    }

    /**
     * Make sure longitude stays within correct bounds.
     * @param longitude
     * @return
     */
    public static double clipLongitude(double longitude) {
        return clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
    }
}
