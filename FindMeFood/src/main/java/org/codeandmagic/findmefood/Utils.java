package org.codeandmagic.findmefood;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

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

    public static QTile[] QTILE_ARR_EMPTY = new QTile[]{};

    /**
     * Find all elements in first array that are not found in the second one.
     *
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> T[] diff(T[] firstArray, T[] secondArray, T[] empty) {
        if (firstArray == null) {
            return empty;
        }
        if (secondArray == null) {
            return firstArray;
        }
        ArrayList<T> diff = new ArrayList<T>();
        for (T e1 : firstArray) {
            boolean found = false;
            for (int i = 0; i < secondArray.length; ++i) {
                if (e1.equals(secondArray[i])) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                diff.add(e1);
            }
        }
        return diff.toArray(empty);
    }

    public static <T> String print(T[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        final StringBuilder builder = new StringBuilder(array.getClass().getSimpleName()).append("[");
        for (T elem : array) {
            builder.append(elem.toString()).append(", ");
        }
        builder.deleteCharAt(builder.length() - 1).append("]");
        return builder.toString();
    }

}
