package org.codeandmagic.findmefood;

import org.codeandmagic.findmefood.model.Place;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by evelyne24.
 */
public class LocationCache {


    private final Map<String, Set<Place>> cache = new HashMap<String, Set<Place>>();
    private final Map<String, Boolean> tilesInCache = new HashMap<String, Boolean>();

    public void add(String quadKey, Place place) {
        Set<Place> places = cache.get(quadKey);
        if (places == null) {
            places = new HashSet<Place>();
            cache.put(quadKey, places);
        }
        places.add(place);
    }

    public Set<Place> get(String quadKey) {
        return cache.get(quadKey);
    }

//    public Set<Place> get(LatLng latLng, ZoomLevel zoomLevel) {
//        final String quadKey = QuadKey.getQuadKey(latLng, zoomLevel);
//        return cache.get(quadKey);
//    }

    public boolean hasTileInCache(QTile qTile) {
        return tilesInCache.get(qTile.quadKey) != null;
    }

    public void addTileInCache(QTile qTile) {
        tilesInCache.put(qTile.quadKey, true);
    }
}
