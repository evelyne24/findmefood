package org.codeandmagic.findmefood;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import static org.codeandmagic.findmefood.LocationUtils.*;

/**
 * Created by evelyne24.
 */
public class LocationCache {

    private final Map<QTile, Marker[]> cache = new HashMap<QTile, Marker[]>();


    public void add(QTile qtile, Marker[] markers) {
        cache.put(qtile, markers);
    }

    public Marker[] get(QTile qTile) {
        return cache.get(qTile);
    }

    public Marker[] get(LatLng latLng) {
        Point point = QuadKey.worldPointToTileXY(QuadKey.latLngToWorldPoint(latLng, ZoomLevel.get(DATA_FETCH_ZOOM_LEVEL)));
        for(QTile qTile : cache.keySet()) {
            if(qTile.x == point.x && qTile.y == point.y && qTile.zoom.zoom == DATA_FETCH_ZOOM_LEVEL) {
                return cache.get(qTile);
            }
        }
        return null;
    }
}
