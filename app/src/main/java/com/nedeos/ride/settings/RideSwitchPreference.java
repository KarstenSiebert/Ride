package com.nedeos.ride.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Karsten on 02.03.2016.
 */
public class RideSwitchPreference extends SwitchPreference {

    public RideSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RideSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RideSwitchPreference(Context context) {
        super(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ViewGroup viewGroup = (ViewGroup) view;
        clearListenerInViewGroup(viewGroup);

        Typeface fontStyle = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

        TextView titleView = view.findViewById(android.R.id.title);

        titleView.setTypeface(fontStyle);

        TextView summeryView = view.findViewById(android.R.id.summary);

        summeryView.setTypeface(fontStyle);
    }

    private void clearListenerInViewGroup(ViewGroup viewGroup) {

        if (viewGroup == null) {
            return;
        }

        int count = viewGroup.getChildCount();

        for (int n = 0; n < count; ++n) {
            View childView = viewGroup.getChildAt(n);

            if (childView instanceof Switch) {
                final Switch switchView = (Switch) childView;
                switchView.setOnCheckedChangeListener(null);
                return;

            } else if (childView instanceof ViewGroup) {
                ViewGroup childGroup = (ViewGroup) childView;
                clearListenerInViewGroup(childGroup);
            }
        }
    }

}
