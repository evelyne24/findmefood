package org.codeandmagic.findmefood;

import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

import static org.codeandmagic.findmefood.QuadKey.latLngToWorldPoint;
import static org.codeandmagic.findmefood.QuadKey.worldPointToLatLng;
import static org.codeandmagic.findmefood.QuadKey.worldPointToTileXY;
import static org.codeandmagic.findmefood.LocationUtils.*;

/**
 * Created by evelyne24.
 * <p/>
 * Latitude grows from bottom to top, reversed from normal Y coordinate.
 * Longitude grows from left to right.
 */
public class QTile {
    public final int x;
    public final int y;
    public final ZoomLevel zoom;
    public final double radius;
    public final String quadKey;
    // The distance from the edge of the world for the top-left and bottom-right tile corners.
    public final Point worldLeftTop;
    public final Point worldRightBottom;
    public final LatLng topLeft, bottomLeft, topRight, bottomRight, center;
    public QTile topLeftNeighbour, topNeighbour, topRightNeighbour;
    public QTile leftNeighbour, rightNeighbour;
    public QTile bottomLeftNeighbour, bottomNeighbour, bottomRightNeighbour;
    private volatile boolean neighboursWereLoaded = false;


    public QTile(final int x, final int y, final ZoomLevel zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;

        this.worldLeftTop = new Point(x * 256, y * 256);
        this.worldRightBottom = new Point((x + 1) * 256, (y - 1) * 256);

        this.topLeft = worldPointToLatLng(worldLeftTop, zoom);
        this.bottomRight = worldPointToLatLng(worldRightBottom, zoom);

        this.bottomLeft = new LatLng(bottomRight.latitude, topLeft.longitude);
        this.topRight = new LatLng(topLeft.latitude, bottomRight.longitude);

        // (BR - TL) / 2 + TL
        double halfX = (bottomRight.longitude + topLeft.longitude) / 2.0;
        // (TL - BR) / 2 + BR
        double halfY = (bottomRight.latitude + topLeft.latitude) / 2.0;

        this.center = new LatLng(halfY, halfX);

        this.radius = getDistanceBetween(center.latitude, center.longitude, topLeft.latitude, topLeft.longitude);

        this.quadKey = getQuadKey();
    }

    public static QTile forCenter(LatLng latLng, ZoomLevel zoom) {
        final Point center = latLngToWorldPoint(latLng, zoom);
        final Point tile = worldPointToTileXY(center);
        return new QTile(tile.x, tile.y, zoom);
    }

    public QTile neighbour(int x, int y) {
        if (x < 0) {
            x = zoom.maxTiles.x + x;
        } else if (x > zoom.maxTiles.x) {
            x = x - zoom.maxTiles.x;
        }
        if (y < 0) {
            y = zoom.maxTiles.y + y;
        } else if (y > zoom.maxTiles.y) {
            y = y - zoom.maxTiles.y;
        }

        return new QTile(x, y, zoom);
    }

    /**
     * Compute the closest 3 neighbours based on where the latlng is in the current tile.
     *
     * @param latLng
     * @return
     */
    public QTile[] closestNeighbours(LatLng latLng) {
        loadNeighbours();

        // center
        double x = latLng.longitude;
        double y = latLng.latitude;

        double centerX = center.longitude;
        double centerY = center.latitude;

        // Check in what quarter of this tile is the point situated
        if (x < centerX && y < centerY) {
            return new QTile[]{leftNeighbour, bottomLeftNeighbour, bottomNeighbour};
        } else if (x < centerX && y >= centerY) {
            return new QTile[]{leftNeighbour, topLeftNeighbour, topNeighbour};
        } else if (x >= centerX && y < centerY) {
            return new QTile[]{bottomNeighbour, bottomRightNeighbour, rightNeighbour};
        } else { //if(x >= halfX && y >= halfY) {
            return new QTile[]{topNeighbour, topRightNeighbour, rightNeighbour};
        }
    }

    public synchronized void loadNeighbours() {
        if (!neighboursWereLoaded) {
            this.topLeftNeighbour = neighbour(x - 1, y - 1);
            this.topNeighbour = neighbour(x, y - 1);
            this.topRightNeighbour = neighbour(x + 1, y - 1);
            this.leftNeighbour = neighbour(x - 1, y);
            this.rightNeighbour = neighbour(x + 1, y);
            this.bottomLeftNeighbour = neighbour(x - 1, y + 1);
            this.bottomNeighbour = neighbour(x, y + 1);
            this.bottomRightNeighbour = neighbour(x + 1, y + 1);
            neighboursWereLoaded = true;
        }
        //return new QTile[] {topLeftNeighbour, topNeighbour, topRightNeighbour,
        //    rightNeighbour, bottomRightNeighbour, bottomNeighbour,
        //    bottomLeftNeighbour,leftNeighbour};
    }

    public String getQuadKey() {
        return QuadKey.getQuadKey(x, y, zoom.zoom);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QTile{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", zoom=").append(zoom);
        sb.append(", worldLeftTop=").append(worldLeftTop);
        sb.append(", worldRightBottom=").append(worldRightBottom);
        sb.append(", topLeft=").append(topLeft);
        sb.append(", bottomRight=").append(bottomRight);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || QTile.class != o.getClass()) return false;

        final QTile qTile = (QTile) o;

        if (x != qTile.x) return false;
        if (y != qTile.y) return false;
        if (zoom != qTile.zoom) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + zoom.hashCode();
        return result;
    }
}
