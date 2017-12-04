package com.nedeos.ride.messages;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;
import com.nedeos.ride.RideApplication;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Created by Karsten on 06.04.2017.
 */

@GlideModule
public class RideGlideLibraryModule extends LibraryGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        OkHttpClient okHttpClient = RideApplication.getOkHttpClient();

        if (okHttpClient != null) {
            registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
        }
    }
}
