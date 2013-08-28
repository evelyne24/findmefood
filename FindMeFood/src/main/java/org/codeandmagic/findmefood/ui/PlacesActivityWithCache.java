package org.codeandmagic.findmefood.ui;

import android.R;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by evelyne24.
 */
public class PlacesActivityWithCache extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.content,
                    PlacesMapFragmentWithCache.newInstance(Bundle.EMPTY)).commit();
        }
    }
}
