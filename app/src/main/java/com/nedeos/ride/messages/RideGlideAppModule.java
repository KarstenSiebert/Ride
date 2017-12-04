package com.nedeos.ride.messages;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;

/**
 * Created by Karsten on 06.04.2017.
 */

@GlideModule
public class RideGlideAppModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

        builder.setDefaultRequestOptions(new RequestOptions()
                .dontAnimate()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fallback(R.drawable.icon)
                .error(R.drawable.icon)
        );
    }

}
