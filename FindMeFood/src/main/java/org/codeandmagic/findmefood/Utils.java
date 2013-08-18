package org.codeandmagic.findmefood;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;
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

    /**
     * Find all elements in first array that are not found in the second one.
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> T[] diff(T[] firstArray, T[] secondArray) {
        ArrayList<T> diff = new ArrayList<T>();
        for(T e1 : firstArray) {
            for (T e2 : secondArray) {
                if(!e1.equals(e2)) {
                    diff.add(e1);
                }
            }
        }
        return (T[]) diff.toArray();
    }

    /**
     * Find all elements that are found in both arrays.
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> T[] intersect(T[] firstArray, T[] secondArray) {
        ArrayList<T> intersect = new ArrayList<T>();
        for(T e1 : firstArray) {
            for (T e2 : secondArray) {
                if(e1.equals(e2)) {
                    intersect.add(e1);
                }
            }
        }
        return (T[]) intersect.toArray();
    }

}
