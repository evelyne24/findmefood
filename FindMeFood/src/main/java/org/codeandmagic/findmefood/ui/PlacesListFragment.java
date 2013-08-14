package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import org.codeandmagic.findmefood.R;
import org.codeandmagic.findmefood.model.Place;
import org.codeandmagic.findmefood.provider.PlacesDatabase;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        decodePlaceTypeIcons();

        getLoaderManager().initLoader(LOAD_PLACES, null, this);
    }

    private ActionBarActivity getActionBarActivity() {
        return (ActionBarActivity) getActivity();
    }

    private void decodePlaceTypeIcons() {
        iconFood = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_restaurant);
        iconDrink = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_coffee);
    }

    private void showLoadingProgressBar(boolean visible) {
        getActionBarActivity().setSupportProgressBarVisibility(visible);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showLoadingProgressBar(true);
        return new CursorLoader(getActivity(), Places.CONTENT_URI, Places.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (LOAD_PLACES == loader.getId()) {
            if (getListAdapter() == null) {
                setListAdapter(new PlacesCursorAdapter(this));
            }
            ((CursorAdapter) getListAdapter()).swapCursor(cursor);
            showLoadingProgressBar(false);
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
            mapFragment = PlacesMapFragment.newInstance(currentPlace);
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
            holder.getRootView().setOpenNow(place.getOpeningHours().isOpenNow());
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
        private final PlaceListItemView rootView;
        private final TextView titleView;
        private final TextView subtitleView;
        private final TextView distanceView;
        private final TextView priceView;

        public PlaceViewHolder(View view) {
            rootView = (PlaceListItemView) view;
            titleView = (TextView) view.findViewById(R.id.title);
            subtitleView = (TextView) view.findViewById(R.id.subtitle);
            distanceView = (TextView) view.findViewById(R.id.distance);
            priceView = (TextView) view.findViewById(R.id.price);
        }

        private PlaceListItemView getRootView() {
            return rootView;
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
}
