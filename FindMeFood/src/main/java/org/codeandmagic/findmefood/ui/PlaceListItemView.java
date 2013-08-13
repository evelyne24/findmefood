package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import org.codeandmagic.findmefood.R;

/**
 * Created by evelyne24.
 */
public class PlaceListItemView extends RelativeLayout {

    private static final int[] STATE_OPEN_NOW = {R.attr.state_open_now};
    private boolean isOpenNow;

    public PlaceListItemView(Context context) {
        super(context);
    }

    public PlaceListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaceListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the Place is open now then we merge our custom open now state into
        // the existing drawable state before returning it.
        if (isOpenNow) {
            // We are going to add 1 extra state.
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_OPEN_NOW);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public boolean isOpenNow() {
        return isOpenNow;
    }

    public void setOpenNow(boolean openNow) {
        if (this.isOpenNow != openNow) {
            isOpenNow = openNow;
            // Refresh the drawable state so that it includes the open now state if required.
            refreshDrawableState();
        }
    }
}
