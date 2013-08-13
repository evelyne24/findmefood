package org.codeandmagic.findmefood.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by evelyne24.
 */
public class Place {

    public static final String TYPE_BAR = "bar";
    public static final String TYPE_CAFE = "cafe";
    public static final String TYPE_FOOD = "food";
    public static final String TYPE_RESTAURANT = "restaurant";

    public static final String[] DEFAULT_TYPES = {TYPE_BAR, TYPE_CAFE, TYPE_FOOD, TYPE_RESTAURANT};
    public static final double DEFAULT_RADIUS = 1609.344; // 1 mile in meters
    public static final String TYPES_DELIMITER = "|";

    private String id;

    private String name;

    @SerializedName("icon")
    private String iconUrl;

    private String vicinity;

    private List<String> types;

    private double rating;

    private int priceLevel;

    private PlaceGeometry geometry;

    @SerializedName("opening_hours")
    private OpeningHours openingHours;

    public Place() {
        openingHours = new OpeningHours();
        openingHours.setOpenNow(false);
        types = Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public PlaceGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(PlaceGeometry geometry) {
        this.geometry = geometry;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }
}
