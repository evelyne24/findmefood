package org.codeandmagic.findmefood.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import org.codeandmagic.findmefood.R;

/**
 * Created by evelyne24.
 */
public class PlaceListItemView extends RelativeLayout {

    //private static final int[] STATE_OPEN_NOW = {R.attr.state_open_now};
    private boolean isOpenNow;
    private int stripeWidth;
    private Paint stripePaint;
    private RectF stripeRect;

    public PlaceListItemView(Context context) {
        super(context);
        init();
    }

    public PlaceListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlaceListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        Resources res = getContext().getResources();
        stripeWidth = res.getDimensionPixelSize(R.dimen.place_open_now_stripe_width);
        stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripePaint.setStyle(Paint.Style.FILL);
        stripePaint.setColor(res.getColor(R.color.place_open_now_stripe));
    }


    //    @Override
//    protected int[] onCreateDrawableState(int extraSpace) {
//        // If the Place is open now then we merge our custom open now state into
//        // the existing drawable state before returning it.
//        if (isOpenNow) {
//            // We are going to add 1 extra state.
//            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
//            mergeDrawableStates(drawableState, STATE_OPEN_NOW);
//            return drawableState;
//        } else {
//            return super.onCreateDrawableState(extraSpace);
//        }
//    }


    @Override
    protected void onDraw(final Canvas canvas) {
        if (isOpenNow) {
            canvas.drawRect(0, 0, stripeWidth, getHeight(), stripePaint);
        }
    }

    public boolean isOpenNow() {
        return isOpenNow;
    }

    public void setOpenNow(boolean openNow) {
        if (this.isOpenNow != openNow) {
            isOpenNow = openNow;
            // Refresh the drawable state so that it includes the open now state if required.
            //refreshDrawableState();
        }
        invalidate();
    }
}
