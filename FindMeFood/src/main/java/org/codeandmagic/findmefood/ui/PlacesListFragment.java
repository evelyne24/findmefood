package org.codeandmagic.findmefood.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.codeandmagic.findmefood.Consts.Intents.EXTRA_PLACE;
import static org.codeandmagic.findmefood.Consts.KILOMETERS;
import static org.codeandmagic.findmefood.Consts.Loaders.LOAD_PLACES;
import static org.codeandmagic.findmefood.Consts.METERS;
import static org.codeandmagic.findmefood.Consts.SPACE;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.MY_LOCATION;
import static org.codeandmagic.findmefood.Consts.SavedInstanceState.PLACES;

/**
 * Created by evelyne24.
 */
public class PlacesListFragment extends ListFragment implements LoaderCallbacks<Cursor>, MyLocationUpdateListener {

    private static final String CURRENCY;

    static {
        Currency currency = Currency.getInstance(Locale.getDefault());
        CURRENCY = (currency != null) ? (currency.getSymbol().length() == 1 ? currency.getSymbol() : "$") : "$";
    }

    private Location myLocation;
    private Bitmap iconFood;
    private Bitmap iconDrink;
    private EndlessScrollListener endlessScrollListener;
    private ArrayList<Place> places = new ArrayList<Place>();


    public static PlacesListFragment newInstance(Bundle args) {
        PlacesListFragment fragment = new PlacesListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);
        endlessScrollListener = new EndlessScrollListener();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            myLocation = savedInstanceState.getParcelable(MY_LOCATION);
            places = savedInstanceState.getParcelableArrayList(PLACES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MY_LOCATION, myLocation);
        outState.putParcelableArrayList(PLACES, places);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        decodePlaceTypeIcons();
        ((PlacesActivity) getActivity()).registerLocationUpdateListener(this);
        getListView().setOnScrollListener(endlessScrollListener);
        setListAdapter(new PlacesAdapter(this, places));
        getLoaderManager().initLoader(LOAD_PLACES, null, this);
    }

    @Override
    public void onDestroy() {
        ((PlacesActivity) getActivity()).unregisterLocationUpdateListener(this);
        super.onDestroy();
    }

    private PlacesActivity getPlacesActivity() {
        return (PlacesActivity) getActivity();
    }

    private void decodePlaceTypeIcons() {
        iconFood = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_restaurant);
        iconDrink = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_coffee);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new PlacesLoader(getActivity(), myLocation);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (LOAD_PLACES == loader.getId()) {
            places.clear();
            places.addAll(PlacesDatabase.readPlaces(cursor));
            Collections.sort(places);
            ((PlacesAdapter) getListAdapter()).setPlaces(places);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Place currentPlace = ((PlacesAdapter) getListAdapter()).getItem(position);
        PlacesMapFragment mapFragment = (PlacesMapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_PLACE, currentPlace);
            mapFragment = PlacesMapFragment.newInstance(args);
            getFragmentManager().beginTransaction().replace(R.id.places_list_fragment, mapFragment).addToBackStack(null).commit();
        } else {
            mapFragment.refreshCurrentPlace(currentPlace);
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        myLocation = location;
        getLoaderManager().restartLoader(LOAD_PLACES, null, this);
    }

    private static class PlacesAdapter extends ArrayAdapter<Place> {
        private PlacesListFragment fragment;
        private List<Place> places;

        public PlacesAdapter(PlacesListFragment fragment, List<Place> places) {
            super(fragment.getActivity(), R.layout.place_list_item, places);
            this.fragment = fragment;
            this.places = places;
        }

        public void setPlaces(List<Place> places) {
            this.places = places;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            PlaceViewHolder holder;

            if (view == null) {
                view = View.inflate(getContext(), R.layout.place_list_item, null);
                holder = new PlaceViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (PlaceViewHolder) view.getTag();
            }

            Place place = places.get(position);
            holder.getTitleView().setText(place.getName());
            holder.getDistanceView().setText(formatDistance(place.getDistance()));
            holder.getSubtitleView().setText(formatExtraInfo(place));
            holder.getPriceView().setText(place.formatPriceLevel(CURRENCY));
            holder.getOpenNowView().setOpenNow(place.getOpeningHours().isOpenNow());
            return view;
        }

        private SpannableStringBuilder formatExtraInfo(Place place) {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(place.getVicinity())) {
                builder.append(place.getVicinity()).append(SPACE);
            }
            return formatLocationType(builder.toString(), place.getTypes());
        }

        private SpannableStringBuilder formatLocationType(String text, List<String> types) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (types.contains(Place.TYPE_FOOD) || types.contains(Place.TYPE_RESTAURANT)) {
                addImageSpan(builder, fragment, fragment.iconFood);
            }
            if (types.contains(Place.TYPE_CAFE) || types.contains(Place.TYPE_BAR)) {
                addImageSpan(builder.append(SPACE), fragment, fragment.iconDrink);
            }
            return builder;
        }

        private void addImageSpan(SpannableStringBuilder builder, PlacesListFragment context, Bitmap icon) {
            builder.setSpan(new ImageSpan(context.getActivity(), icon),
                    builder.length() - 1, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        private String formatDistance(double distance) {
            if (Double.compare(distance, 0) == 0) {
                return "...";
            } else if (distance >= 1000) {
                return String.format("%.2f %s", (distance / 1000.0), KILOMETERS);
            } else {
                return String.format("%.2f %s", distance, METERS);
            }
        }
    }

    private static class PlaceViewHolder {
        private final PlaceListItemView view;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView distanceView;
        private final TextView priceView;

        public PlaceViewHolder(View view) {
            this.view = (PlaceListItemView) view;
            titleView = (TextView) view.findViewById(R.id.title);
            subtitleView = (TextView) view.findViewById(R.id.subtitle);
            distanceView = (TextView) view.findViewById(R.id.distance);
            priceView = (TextView) view.findViewById(R.id.price);
        }

        private PlaceListItemView getOpenNowView() {
            return view;
        }

        private TextView getTitleView() {
            return titleView;
        }

        private TextView getSubtitleView() {
            return subtitleView;
        }

        private TextView getDistanceView() {
            return distanceView;
        }

        private TextView getPriceView() {
            return priceView;
        }
    }

    private class EndlessScrollListener implements OnScrollListener {
        // How many items are visible before getting to the bottom
        // so a preemptive load can triggered before that.
        private static final int VISIBLE_THRESHOLD = 5;
        private int previousTotal;
        private boolean loading;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // nothing to do
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //Log.v(APP_TAG, MessageFormat.format("firstVisibleItem={0}, visibleItemCount={1}, totalItemCount={2}, previousTotal={3}",
            //        firstVisibleItem, visibleItemCount, totalItemCount, previousTotal));

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                if (getPlacesActivity().hasNextPage()) {
                    getPlacesActivity().requestNextPage();
                    loading = true;
                }
            }
        }
    }
}
