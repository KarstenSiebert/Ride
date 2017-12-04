package com.nedeos.ride.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.nedeos.ride.R;

/**
 * Created by Karsten on 03.04.2016.
 */
public class RideEditTextPreference extends EditTextPreference {

    public RideEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RideEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RideEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        final Window window = getDialog().getWindow();

        if (window != null) {
            final Button button1 = window.findViewById(getContext().getResources().getIdentifier("button1", "id", "android"));

            if (button1 != null) {
                button1.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
            }

            final Button button2 = window.findViewById(getContext().getResources().getIdentifier("button2", "id", "android"));

            if (button2 != null) {
                button2.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
            }
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Typeface fontStyle = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

        TextView titleView = view.findViewById(android.R.id.title);

        titleView.setTypeface(fontStyle);

        TextView summeryView = view.findViewById(android.R.id.summary);

        summeryView.setTypeface(fontStyle);
    }

}
