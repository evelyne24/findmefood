package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;
import org.codeandmagic.findmefood.service.PlacesUpdateService;

import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.codeandmagic.findmefood.Consts.APP_TAG;
import static org.codeandmagic.findmefood.Consts.Intents.*;
import static org.codeandmagic.findmefood.Consts.Loaders.LOAD_PLACES;
import static org.codeandmagic.findmefood.Consts.SPACE;
import static org.codeandmagic.findmefood.provider.PlacesDatabase.Places;

/**
 * Created by evelyne24.
 */
public class PlacesListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    private static final String CURRENCY;

    static {
        Currency currency = Currency.getInstance(Locale.getDefault());
        CURRENCY = (currency != null) ? (currency.getSymbol().length() == 1 ? currency.getSymbol() : "$") : "$";
    }

    private Bitmap iconFood;
    private Bitmap iconDrink;
    private EndlessScrollListener endlessScrollListener;

    public static PlacesListFragment newInstance(Bundle args) {
        PlacesListFragment fragment = new PlacesListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endlessScrollListener = new EndlessScrollListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        decodePlaceTypeIcons();
        getLoaderManager().initLoader(LOAD_PLACES, null, this);
        getListView().setOnScrollListener(endlessScrollListener);
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
        return new CursorLoader(getActivity(), Places.CONTENT_URI, Places.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (LOAD_PLACES == loader.getId()) {
            if (getListAdapter() == null) {
                setListAdapter(new PlacesCursorAdapter(this));
            }
            ((CursorAdapter) getListAdapter()).swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (LOAD_PLACES == loader.getId()) {
            ((CursorAdapter) getListAdapter()).swapCursor(null);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Place currentPlace = PlacesDatabase.readPlace((Cursor) getListAdapter().getItem(position));
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

    private static class PlacesCursorAdapter extends CursorAdapter {
        private PlacesListFragment context;

        public PlacesCursorAdapter(PlacesListFragment context) {
            super(context.getActivity(), null, 0);
            this.context = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = View.inflate(context, R.layout.place_list_item, null);
            view.setTag(new PlaceViewHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Place place = PlacesDatabase.readPlace(cursor);
            PlaceViewHolder holder = (PlaceViewHolder) view.getTag();
            holder.getTitleView().setText(place.getName());
            holder.getDistanceView().setText("1 mile");
            holder.getSubtitleView().setText(formatExtraInfo(place));
            holder.getPriceView().setText(place.formatPriceLevel(CURRENCY));
            holder.getOpenNowView().setVisibility(place.getOpeningHours().isOpenNow() ? View.VISIBLE : View.GONE);
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
                addImageSpan(builder, context, context.iconFood);
            }
            if (types.contains(Place.TYPE_CAFE) || types.contains(Place.TYPE_BAR)) {
                addImageSpan(builder.append(SPACE), context, context.iconDrink);
            }
            return builder;
        }

        private void addImageSpan(SpannableStringBuilder builder, PlacesListFragment context, Bitmap icon) {
            builder.setSpan(new ImageSpan(context.getActivity(), icon),
                    builder.length() - 1, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private static class PlaceViewHolder {
        private final View openNowView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView distanceView;
        private final TextView priceView;

        public PlaceViewHolder(View view) {
            openNowView = view.findViewById(R.id.open_now);
            titleView = (TextView) view.findViewById(R.id.title);
            subtitleView = (TextView) view.findViewById(R.id.subtitle);
            distanceView = (TextView) view.findViewById(R.id.distance);
            priceView = (TextView) view.findViewById(R.id.price);
        }

        private View getOpenNowView() {
            return openNowView;
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

        private static final int VISIBLE_THRESHOLD = 1;
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
