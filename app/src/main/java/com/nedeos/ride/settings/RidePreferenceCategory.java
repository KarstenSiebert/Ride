package com.nedeos.ride.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Karsten on 01.09.2017.
 */

public class RidePreferenceCategory extends PreferenceCategory {

    public RidePreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RidePreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RidePreferenceCategory(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Typeface fontStyle = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

        TextView titleView = view.findViewById(android.R.id.title);

        titleView.setTypeface(fontStyle);
    }

}
