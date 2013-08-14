package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
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

import java.util.Currency;
import java.util.Locale;

import static org.codeandmagic.findmefood.Consts.Loaders.LOAD_PLACES;
import static org.codeandmagic.findmefood.Consts.SP;
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
        getLoaderManager().initLoader(LOAD_PLACES, null, this);
        decodePlaceTypeIcons();
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
            String name = cursor.getString(cursor.getColumnIndex(Places.NAME));
            int priceLevel = cursor.getInt(cursor.getColumnIndex(Places.PRICE_LEVEL));
            double rating = cursor.getDouble(cursor.getColumnIndex(Places.RATING));
            boolean openNow = cursor.getInt(cursor.getColumnIndex(Places.OPEN_NOW)) > 0;
            String vicinity = cursor.getString(cursor.getColumnIndex(Places.VICINITY));
            String types = cursor.getString(cursor.getColumnIndex(Places.TYPES));

            PlaceViewHolder holder = (PlaceViewHolder) view.getTag();
            holder.getTitleView().setText(name);
            holder.getDistanceView().setText("1 mile");
            holder.getSubtitleView().setText(formatExtraInfo(vicinity, types, rating));
            holder.getPriceView().setText(formatPriceLevel(priceLevel));
            holder.getRootView().setOpenNow(openNow);
        }

        private SpannableStringBuilder formatExtraInfo(String vicinity, String types, double rating) {
            StringBuilder builder = new StringBuilder();
            if(!TextUtils.isEmpty(vicinity)) {
               builder.append(vicinity).append(SP);
            }
           return formatLocationType(builder.toString(), types);
        }

        private SpannableStringBuilder formatLocationType(String text, String types) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if(types.contains(Place.TYPE_FOOD) || types.contains(Place.TYPE_RESTAURANT)) {
                addImageSpan(builder, context, context.iconFood);
            }
            if(types.contains(Place.TYPE_CAFE) || types.contains(Place.TYPE_BAR)) {
                addImageSpan(builder.append(SP), context, context.iconDrink);
            }
            return builder;
        }

        private void addImageSpan(SpannableStringBuilder builder, PlacesListFragment context, Bitmap icon) {
            builder.setSpan(new ImageSpan(context.getActivity(), icon),
                    builder.length() - 1, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        /** The price level is between 0 and 4.
         * We show as many currency symbols as the price level.
         * For 0 we show 1 currency symbol.
         */
        private String formatPriceLevel(int priceLevel) {
            int length = (priceLevel == 0) ? 1 : priceLevel;
            return String.format("%" + length + "s", CURRENCY).replaceAll(SP, CURRENCY);
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Fragment mapFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if(mapFragment == null) {
            mapFragment = PlacesMapFragment.instantiate(getActivity(), PlacesMapFragment.class.getName());
            getFragmentManager().beginTransaction().replace(R.id.places_list_fragment, mapFragment).addToBackStack(null).commit();
        }
        else {
            //TODO refresh existing fragment
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
