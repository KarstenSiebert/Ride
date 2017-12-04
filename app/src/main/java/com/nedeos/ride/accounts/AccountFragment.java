package com.nedeos.ride.accounts;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;
import com.nedeos.ride.messages.GlideApp;
import com.nedeos.ride.services.ChangeImageService;
import com.nedeos.ride.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class AccountFragment extends Fragment {

    private OnAccountListener mAccountListener;

    SharedPreferences preferences;

    ImageView mImage;

    ImageView mAccountImage1;
    ImageView mAccountImage2;
    ImageView mAccountImage3;

    private Bitmap mAvatar;

    private Bitmap mImage1;
    private Bitmap mImage2;
    private Bitmap mImage3;

    private Uri dateiName;

    private ArrayList<User> userList;

    private AccountAdapter accountAdapter;

    private RecyclerView recyclerView;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();

        Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (userList == null) {
            userList = new ArrayList<>();

        } else {
            userList.clear();
        }

        accountAdapter = new AccountAdapter(this, userList);

        if (savedInstanceState != null) {
            userList = savedInstanceState.getParcelableArrayList(RideApplication.ACCOUNTFRAGMENT_ACCOUNT_LIST);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putParcelableArrayList(RideApplication.ACCOUNTFRAGMENT_ACCOUNT_LIST, userList);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (userList == null) {
            userList = new ArrayList<>();

        } else {
            userList.clear();
        }

        if ((recyclerView != null) && (accountAdapter != null)) {
            recyclerView.getRecycledViewPool().clear();

            accountAdapter.notifyDataSetChanged();
        }

        if ((preferences != null) && (userList != null) && (userList.size() == 0)) {

            try {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("identify", preferences.getString(RideApplication.ARG_IDENTIFY, ""));
                jsonObject.put("password", preferences.getString(RideApplication.ARG_PASSWORD, ""));

                OkHttpClient client = RideApplication.getOkHttpClient();

                if (client != null) {
                    RequestBody body = RequestBody.create(RideApplication.JSON, jsonObject.toString());

                    Request request = new Request.Builder()
                            .url("https://ride.barubox.com/listallFavor")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()) {

                                try {
                                    JSONArray users = new JSONArray(response.body().string());

                                    if (users.length() > 0) {

                                        for (int i = 0; i < users.length(); i++) {
                                            JSONObject user = users.optJSONObject(i);

                                            final User item = new User();


                                            item.setUsid(user.optInt(RideApplication.ARG_USID));

                                            item.setHead(user.optString(RideApplication.ARG_HEAD));
                                            item.setIcon(user.optString(RideApplication.ARG_ICON));

                                            item.setImage_1(user.optString(RideApplication.ARG_IMAGE_1));
                                            item.setImage_2(user.optString(RideApplication.ARG_IMAGE_2));
                                            item.setImage_3(user.optString(RideApplication.ARG_IMAGE_3));

                                            Handler handler = new Handler(Looper.getMainLooper());

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    accountAdapter.addItem(item);
                                                }
                                            });
                                        }
                                    }

                                } catch (JSONException e) {
                                    //e.printStackTrace();
                                }
                            }
                        }
                    });
                }

            } catch (JSONException e) {
                // e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        recyclerView = view.findViewById(R.id.account_recyclerview);

        recyclerView.setClickable(true);

        recyclerView.isFocusable();

        recyclerView.setFocusableInTouchMode(true);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(accountAdapter);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        AccountLayoutManager layoutManager = new AccountLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        layoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        layoutManager.setExtraLayoutSpace(size.x);
        layoutManager.setAutoMeasureEnabled(true);

        layoutManager.setAutoMeasureEnabled(true);

        recyclerView.setLayoutManager(layoutManager);

        mImage = view.findViewById(R.id.account_Image);

        if (mImage != null) {
            GlideApp.get(getActivity()).clearMemory();

            RequestOptions requestOptions = new RequestOptions()
                    .circleCrop()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            File sourceFile = new File(getActivity().getFilesDir(), "avatar.jpg");

            if (sourceFile.exists() && (sourceFile.length() > 0)) {
                GlideApp.with(this).load(sourceFile)
                        .apply(requestOptions)
                        .into(mImage);

            } else {
                GlideApp.with(this).load(R.drawable.icon)
                        .apply(requestOptions)
                        .into(mImage);
            }

            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    dateiName = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, dateiName);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_SET_PHOTO);

                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });

            mImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_GET_PHOTO);

                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    }

                    return true;
                }
            });
        }

        mAccountImage1 = view.findViewById(R.id.account_Image_1);

        if (mAccountImage1 != null) {
            GlideApp.get(getActivity()).clearMemory();

            RequestOptions requestOptions = new RequestOptions()
                    .override(80, 60)
                    .fitCenter()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            File sourceFile = new File(getActivity().getFilesDir(), "image1.jpg");

            if (sourceFile.exists() && (sourceFile.length() > 0)) {
                GlideApp.with(this).load(sourceFile)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage1);

            } else {
                GlideApp.with(this).load(R.drawable.icon)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage1);
            }

            mAccountImage1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    dateiName = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, dateiName);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_SET_PHOTO_IMAGE1);

                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });

            mAccountImage1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_GET_PHOTO_IMAGE1);

                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    }

                    return true;
                }
            });
        }

        mAccountImage2 = view.findViewById(R.id.account_Image_2);

        if (mAccountImage2 != null) {
            GlideApp.get(getActivity()).clearMemory();

            RequestOptions requestOptions = new RequestOptions()
                    .override(80, 60)
                    .fitCenter()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            File sourceFile = new File(getActivity().getFilesDir(), "image2.jpg");

            if (sourceFile.exists() && (sourceFile.length() > 0)) {
                GlideApp.with(this).load(sourceFile)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage2);

            } else {
                GlideApp.with(this).load(R.drawable.icon)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage2);
            }

            mAccountImage2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    dateiName = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, dateiName);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_SET_PHOTO_IMAGE2);

                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });

            mAccountImage2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_GET_PHOTO_IMAGE2);

                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    }

                    return true;
                }
            });
        }

        mAccountImage3 = view.findViewById(R.id.account_Image_3);

        if (mAccountImage3 != null) {
            GlideApp.get(getActivity()).clearMemory();

            RequestOptions requestOptions = new RequestOptions()
                    .override(80, 60)
                    .fitCenter()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            File sourceFile = new File(getActivity().getFilesDir(), "image3.jpg");

            if (sourceFile.exists() && (sourceFile.length() > 0)) {
                GlideApp.with(this).load(sourceFile)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage3);

            } else {
                GlideApp.with(this).load(R.drawable.icon)
                        .apply(requestOptions)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(mAccountImage3);
            }

            mAccountImage3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    dateiName = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, dateiName);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_SET_PHOTO_IMAGE3);

                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }
            });

            mAccountImage3.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                        try {
                            startActivityForResult(intent, RideApplication.ACCOUNT_GET_PHOTO_IMAGE3);

                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    }

                    return true;
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.my_account);
        }

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.appbar);

        if (actionBar != null) {
            appBarLayout.setExpanded(true, true);
        }

        if (mImage != null) {
            AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

            animation.setDuration(1000);
            animation.setStartOffset(0);

            mImage.setVisibility(View.VISIBLE);
            mImage.startAnimation(animation);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onPressed(Uri uri) {

        if (mAccountListener != null) {
            mAccountListener.onAccountFragment(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnAccountListener) {
            mAccountListener = (OnAccountListener) context;

        } else {
            throw new RuntimeException(context.toString() + " must implement OnAccountListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAccountListener = null;
    }

    public interface OnAccountListener {
        void onAccountFragment(Uri uri);
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        String timeStamp = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(new Date());

        File mediaFile;
        boolean success;

        if (type == MEDIA_TYPE_IMAGE) {
            File mediaStoreDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");

            success = mediaStoreDir.exists() || mediaStoreDir.mkdir();

            if (success) {
                mediaFile = new File(mediaStoreDir.getPath() + File.separator + "Ride" + timeStamp + ".jpg");

            } else {
                mediaFile = null;
            }

        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RideApplication.ACCOUNT_SET_PHOTO:

                if (resultCode == Activity.RESULT_OK) {

                    if (dateiName != null) {

                        try {
                            if (mAvatar != null) {
                                mAvatar.recycle();
                            }

                            mAvatar = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(dateiName), null, null);

                            if ((mAvatar != null) && (mImage != null) && (mAvatar.getWidth() != 0) && (mAvatar.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mAvatar.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mAvatar.getHeight());

                                int width = Math.round(ratio * (float) mAvatar.getWidth());
                                int height = Math.round(ratio * (float) mAvatar.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mAvatar = Bitmap.createScaledBitmap(mAvatar, width, height, true);

                                    if (mAvatar != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .circleCrop()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mAvatar)
                                                .apply(requestOptions)
                                                .into(mImage);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "avatar.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mAvatar.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 0);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_GET_PHOTO:

                if (resultCode == Activity.RESULT_OK) {

                    if (data.getData() != null) {

                        try {
                            if (mAvatar != null) {
                                mAvatar.recycle();
                            }

                            mAvatar = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()), null, null);

                            if ((mAvatar != null) && (mImage != null) && (mAvatar.getWidth() != 0) && (mAvatar.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mAvatar.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mAvatar.getHeight());

                                int width = Math.round(ratio * (float) mAvatar.getWidth());
                                int height = Math.round(ratio * (float) mAvatar.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mAvatar = Bitmap.createScaledBitmap(mAvatar, width, height, true);

                                    if (mAvatar != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .circleCrop()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mAvatar)
                                                .apply(requestOptions)
                                                .into(mImage);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "avatar.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mAvatar.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 0);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_SET_PHOTO_IMAGE1:

                if (resultCode == Activity.RESULT_OK) {

                    if (dateiName != null) {

                        try {
                            if (mImage1 != null) {
                                mImage1.recycle();
                            }

                            mImage1 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(dateiName), null, null);

                            if ((mImage1 != null) && (mAccountImage1 != null) && (mImage1.getWidth() != 0) && (mImage1.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage1.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage1.getHeight());

                                int width = Math.round(ratio * (float) mImage1.getWidth());
                                int height = Math.round(ratio * (float) mImage1.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage1 = Bitmap.createScaledBitmap(mImage1, width, height, true);

                                    if (mImage1 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage1)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage1);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image1.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage1.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 1);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_GET_PHOTO_IMAGE1:

                if (resultCode == Activity.RESULT_OK) {

                    if (data.getData() != null) {

                        try {
                            if (mImage1 != null) {
                                mImage1.recycle();
                            }

                            mImage1 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()), null, null);

                            if ((mImage1 != null) && (mAccountImage1 != null) && (mImage1.getWidth() != 0) && (mImage1.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage1.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage1.getHeight());

                                int width = Math.round(ratio * (float) mImage1.getWidth());
                                int height = Math.round(ratio * (float) mImage1.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage1 = Bitmap.createScaledBitmap(mImage1, width, height, true);

                                    if (mImage1 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage1)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage1);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image1.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage1.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 1);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_SET_PHOTO_IMAGE2:

                if (resultCode == Activity.RESULT_OK) {

                    if (dateiName != null) {

                        try {
                            if (mImage2 != null) {
                                mImage2.recycle();
                            }

                            mImage2 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(dateiName), null, null);

                            if ((mImage2 != null) && (mAccountImage2 != null) && (mImage2.getWidth() != 0) && (mImage2.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage2.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage2.getHeight());

                                int width = Math.round(ratio * (float) mImage2.getWidth());
                                int height = Math.round(ratio * (float) mImage2.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage2 = Bitmap.createScaledBitmap(mImage2, width, height, true);

                                    if (mImage2 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage2)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage2);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image2.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage2.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 2);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_GET_PHOTO_IMAGE2:

                if (resultCode == Activity.RESULT_OK) {

                    if (data.getData() != null) {

                        try {
                            if (mImage2 != null) {
                                mImage2.recycle();
                            }

                            mImage2 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()), null, null);

                            if ((mImage2 != null) && (mAccountImage2 != null) && (mImage2.getWidth() != 0) && (mImage2.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage2.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage2.getHeight());

                                int width = Math.round(ratio * (float) mImage2.getWidth());
                                int height = Math.round(ratio * (float) mImage2.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage2 = Bitmap.createScaledBitmap(mImage2, width, height, true);

                                    if (mImage2 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage2)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage2);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image2.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage2.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 2);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_SET_PHOTO_IMAGE3:

                if (resultCode == Activity.RESULT_OK) {

                    if (dateiName != null) {

                        try {
                            if (mImage3 != null) {
                                mImage3.recycle();
                            }

                            mImage3 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(dateiName), null, null);

                            if ((mImage3 != null) && (mAccountImage3 != null) && (mImage3.getWidth() != 0) && (mImage3.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage3.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage3.getHeight());

                                int width = Math.round(ratio * (float) mImage3.getWidth());
                                int height = Math.round(ratio * (float) mImage3.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage3 = Bitmap.createScaledBitmap(mImage3, width, height, true);

                                    if (mImage3 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage3)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage3);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image3.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage3.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 3);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;

            case RideApplication.ACCOUNT_GET_PHOTO_IMAGE3:

                if (resultCode == Activity.RESULT_OK) {

                    if (data.getData() != null) {

                        try {
                            if (mImage3 != null) {
                                mImage3.recycle();
                            }

                            mImage3 = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()), null, null);

                            if ((mImage3 != null) && (mAccountImage3 != null) && (mImage3.getWidth() != 0) && (mImage3.getHeight() != 0)) {
                                float ratio = Math.min((float) RideApplication.MAX_IMAGE_SIZE / mImage3.getWidth(), (float) RideApplication.MAX_IMAGE_SIZE / mImage3.getHeight());

                                int width = Math.round(ratio * (float) mImage3.getWidth());
                                int height = Math.round(ratio * (float) mImage3.getHeight());

                                if ((width != 0) && (height != 0)) {
                                    mImage3 = Bitmap.createScaledBitmap(mImage3, width, height, true);

                                    if (mImage3 != null) {
                                        GlideApp.get(getActivity()).clearMemory();

                                        RequestOptions requestOptions = new RequestOptions()
                                                .override(80, 60)
                                                .fitCenter()
                                                .dontAnimate()
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true)
                                                .fallback(R.drawable.icon)
                                                .error(R.drawable.icon);

                                        GlideApp.with(this).load(mImage3)
                                                .apply(requestOptions)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                                                .into(mAccountImage3);

                                        FileOutputStream fos;

                                        try {
                                            File sourceFile = new File(getActivity().getFilesDir(), "image3.jpg");

                                            fos = new FileOutputStream(sourceFile);

                                            mImage3.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                                            fos.close();

                                            if (sourceFile.exists() && (sourceFile.length() > 0)) {

                                                Intent imageService = new Intent(getActivity(), ChangeImageService.class);
                                                imageService.putExtra(RideApplication.ARG_IMAGE_NUMBER, 3);
                                                getActivity().startService(imageService);
                                            }

                                        } catch (Exception e) {
                                            // e.printStackTrace();
                                        }
                                    }
                                }
                            }

                        } catch (FileNotFoundException e) {
                            // e.printStackTrace();
                        }
                    }
                }

                break;
        }
    }

}
