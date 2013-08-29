package org.codeandmagic.findmefood.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.codeandmagic.findmefood.Consts.SPACE;

/**
 * Created by evelyne24.
 */
public class Place implements Parcelable, Comparable<Place> {

    private static final String TYPES_DELIMITER = "|";

    public static final String TYPE_BAR = "bar";
    public static final String TYPE_CAFE = "cafe";
    public static final String TYPE_FOOD = "food";
    public static final String TYPE_RESTAURANT = "restaurant";

    public static final String DEFAULT_TYPES = TextUtils.join(TYPES_DELIMITER,
            new String[]{TYPE_BAR, TYPE_CAFE, TYPE_FOOD, TYPE_RESTAURANT});

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

    private double distance;

    private transient LatLng latLng;

    public Place() {
        openingHours = new OpeningHours();
        openingHours.setOpenNow(false);
        types = Collections.emptyList();
    }

    public Place(Parcel in) {
        id = in.readString();
        name = in.readString();
        vicinity = in.readString();
        iconUrl = in.readString();
        setTypes(in.readString());
        rating = in.readDouble();
        priceLevel = in.readInt();
        openingHours = in.readParcelable(OpeningHours.class.getClassLoader());
        geometry = in.readParcelable(PlaceGeometry.class.getClassLoader());
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

    public String getTypesAsString() {
        return joinTypes(types);
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setTypes(String types) {
        this.types = Arrays.asList(TextUtils.split(types, TYPES_DELIMITER));
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

    /**
     * We show as many currency symbols as the price level.
     * @param currency
     * @return
     */
    public String formatPriceLevel(String currency) {
        int length = (priceLevel == 0) ? 1 : priceLevel;
        return String.format("%" + length + "s", currency).replaceAll(SPACE, currency);
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

    public static String joinTypes(List<String> types) {
        return TextUtils.join(TYPES_DELIMITER, types);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public synchronized LatLng getLatLng() {
        if(latLng == null) {
            latLng = new LatLng(geometry.getLocation().getLatitude(), geometry.getLocation().getLongitude());
        }
        return latLng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Place.class != o.getClass()) return false;

        Place place = (Place) o;

        if (!id.equals(place.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(vicinity);
        dest.writeString(iconUrl);
        dest.writeString(getTypesAsString());
        dest.writeDouble(rating);
        dest.writeInt(priceLevel);
        dest.writeParcelable(openingHours, 0);
        dest.writeParcelable(geometry, 0);
    }

    public static final Parcelable.Creator<Place> CREATOR
            = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Place{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", iconUrl='").append(iconUrl).append('\'');
        sb.append(", vicinity='").append(vicinity).append('\'');
        sb.append(", types=").append(types);
        sb.append(", rating=").append(rating);
        sb.append(", priceLevel=").append(priceLevel);
        sb.append(", geometry=").append(geometry);
        sb.append(", openingHours=").append(openingHours);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Place another) {
        return Double.compare(distance, another.getDistance());
    }
}
