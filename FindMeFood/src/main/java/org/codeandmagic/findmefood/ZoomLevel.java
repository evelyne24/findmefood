package org.codeandmagic.findmefood;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

import static org.codeandmagic.findmefood.LocationUtils.clip;

/**
 * Created by evelyne24.
 */
public enum ZoomLevel {

    Z0(0, 256, 156543.034),
    Z1(1, 512, 78271.5170),
    Z2(2, 1024, 39135.7585),
    Z3(3, 2048, 19567.8792),
    Z4(4, 4096, 9783.9396),
    Z5(5, 8192, 4891.9698),
    Z6(6, 16384, 2445.9849),
    Z7(7, 32768, 1222.99),
    Z8(8, 65536, 611.4962),
    Z9(9, 131072, 305.7481),
    Z10(10, 262144, 152.8741),
    Z11(11, 524288, 76.4370),
    Z12(12, 1048576, 38.2185),
    Z13(13, 2097152, 19.1093),
    Z14(14, 4194304, 9.5546),
    Z15(15, 8388608, 4.7773),
    Z16(16, 16777216, 2.3887),
    Z17(17, 33554432, 1.1943),
    Z18(18, 67108864, 0.5972),
    Z19(19, 134217728, 0.2986);

    public static ZoomLevel get(int zoom) {
        return ZoomLevel.values()[clip(zoom, 0, 19)];
    }

    public final int zoom;
    public final int mapSize;
    public final double metersPerPixel;
    public final Point maxTiles;

    ZoomLevel(int zoom, int mapSize, double metersPerPixel) {
        this.zoom = zoom;
        this.mapSize = mapSize;
        this.metersPerPixel = metersPerPixel;
        maxTiles = QuadKey.latLngToWorldPoint(new LatLng(85.05112877, 179.999), this);
    }

}
