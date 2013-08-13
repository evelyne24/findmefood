package org.codeandmagic.findmefood.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by evelyne24.
 */
public class OpeningHours {

    @SerializedName("open_now")
    private boolean openNow;

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}
