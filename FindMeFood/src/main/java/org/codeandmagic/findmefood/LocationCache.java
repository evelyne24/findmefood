package org.codeandmagic.findmefood;

import org.codeandmagic.findmefood.model.Place;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by evelyne24.
 */
public class LocationCache {

    private final Map<String, Collection<Place>> cache = new HashMap<String, Collection<Place>>();
    private boolean multiLevelCache = true;

    public void add(QTile qTile, Collection<Place> places) {
        cache.put(qTile.quadKey, places);
    }

    public Collection<Place> get(QTile qTile) {
        final String quadKey = qTile.quadKey;

        if (multiLevelCache) {
            return cache.get(quadKey);
        } else {
            // Try to find an exact match
            Collection<Place> match = cache.get(quadKey);
            if (match != null) {
                return match;
            }

            // Try to find a parent tile that has cached locations
            for (String maybeParentQuadKey : cache.keySet()) {
                if (quadKey.startsWith(maybeParentQuadKey)) {
                    return cache.get(maybeParentQuadKey);
                }
            }
            // not found
            return null;
        }
    }
}
