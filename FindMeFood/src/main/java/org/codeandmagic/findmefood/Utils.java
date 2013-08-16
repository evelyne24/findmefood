package org.codeandmagic.findmefood;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static org.codeandmagic.findmefood.Consts.UserSettings.DEFAULT_RADIUS;
import static org.codeandmagic.findmefood.Consts.UserSettings.FRESH_LOCATION_INTERVAL;
import static org.codeandmagic.findmefood.Consts.IE6;

/**
 * Created by evelyne24.
 */
public final class Utils {

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean isActiveNetworkWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && ConnectivityManager.TYPE_WIFI == activeNetwork.getType();
    }

    public static float getDistanceBetween(Location startLocation, Location endLocation) {
        return getDistanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
                endLocation.getLatitude(), endLocation.getLongitude());
    }

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
}
