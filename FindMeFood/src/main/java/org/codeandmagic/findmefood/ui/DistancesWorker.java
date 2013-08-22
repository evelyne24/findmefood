package org.codeandmagic.findmefood.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.codeandmagic.findmefood.Utils;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.model.PlaceLocation;

import java.lang.ref.WeakReference;

import static org.codeandmagic.findmefood.Consts.KILOMETERS;
import static org.codeandmagic.findmefood.Consts.METERS;

/**
 * Created by evelyne24 on 20/08/2013.
 */
public class DistancesWorker {

    private LruCache<String, Float> cache;
    private Fragment fragment;

    public DistancesWorker(Fragment fragment) {
        this.fragment = fragment;
        initCache();
    }

    private void initCache() {
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) fragment.getActivity().getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 8;
        cache = new LruCache<String, Float>(cacheSize);
    }

    public void computeDistance(LatLng latLng, Place place, TextView textView) {
        String id = place.getId();

        if (cache.get(id) != null) {
            textView.setText(formatDistance(cache.get(id)));
        } else {
            execute(new DistanceTask(latLng, place, textView));
        }
    }

    private String formatDistance(float distance) {
        if (distance >= 1000) {
            return String.format("%.2f %s", (distance / 1000.0), KILOMETERS);
        } else {
            return String.format("%.2f %s", distance, METERS);
        }
    }

    @TargetApi(11)
    public void execute(DistanceTask task) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
            task.execute();
        } else {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class DistanceTask extends AsyncTask<Void, Void, Float> {

        private LatLng latLng;
        private Place place;
        private WeakReference<TextView> weakReference;

        public DistanceTask(LatLng latLng, Place place, TextView textView) {
            this.latLng = latLng;
            this.place = place;
            this.weakReference = new WeakReference<TextView>(textView);
        }

        @Override
        protected Float doInBackground(Void... params) {
            PlaceLocation placeLocation = place.getGeometry().getLocation();
            float distance = Utils.getDistanceBetween(latLng.latitude, latLng.longitude, placeLocation.getLatitude(), placeLocation.getLongitude());
            cache.put(place.getId(), distance);
            return distance;
        }

        @Override
        protected void onPostExecute(Float distance) {
            TextView textView = weakReference.get();
            if (textView != null) {
                textView.setText(formatDistance(distance));
            }
        }
    }
}
