package com.nedeos.ride.messages;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Karsten on 20.07.2016.
 */
public class RideLinearLayoutManager extends LinearLayoutManager {

    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 600;
    private int extraLayoutSpace = -1;

    public RideLinearLayoutManager(Context context) {
        super(context);
    }

    private RideLinearLayoutManager(Context context, int extraLayoutSpace) {
        super(context);
        this.extraLayoutSpace = extraLayoutSpace;
    }

    public RideLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public RideLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        if (this.extraLayoutSpace > 0) {
            return this.extraLayoutSpace;
        }

        return DEFAULT_EXTRA_LAYOUT_SPACE;
    }

}
