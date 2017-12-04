package com.nedeos.ride.details;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by Karsten on 18.11.2017.
 */

public class DetailImageView extends AppCompatImageView {

    public DetailImageView(Context context) {
        super(context);
    }

    public DetailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

}