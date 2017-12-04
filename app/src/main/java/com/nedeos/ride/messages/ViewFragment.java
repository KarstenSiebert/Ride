package com.nedeos.ride.messages;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.RideApplication;

import java.util.ArrayList;
import java.util.Random;

public class ViewFragment extends Fragment {

    private static int SUCCESSFULLY_REMOVED = 0;

    private MessageNotificationReceiver messageNotificationReceiver;

    private OnViewListener mViewListener;

    private ArrayList<Message> messageList;

    private ViewAdapter viewAdapter;

    private RecyclerView recyclerView;

    private RecyclerView.SmoothScroller smoothScroller;

    private ImageView emptyImage;

    SharedPreferences preferences;

    private boolean useArguments = true;

    public ViewFragment() {
        // Required empty public constructor
    }

    public static ViewFragment newInstance(int layoutPosition) {
        ViewFragment fragment = new ViewFragment();

        Bundle args = new Bundle();

        args.putInt(RideApplication.ARG_LAYOUT_POSITION, layoutPosition);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delOldFromRideDB();

        if (messageList == null) {
            messageList = new ArrayList<>();

        } else {
            messageList.clear();
        }

        viewAdapter = new ViewAdapter(this, messageList);

        if (savedInstanceState == null) {
            getAllFromRideDB();

        } else {
            messageList = savedInstanceState.getParcelableArrayList(RideApplication.VIEWFRAGMENT_MESSAGE_LIST);
        }

        if (messageNotificationReceiver == null) {
            messageNotificationReceiver = new MessageNotificationReceiver();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RideApplication.ARG_MESSAGE_NOTIFICATION);

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageNotificationReceiver, intentFilter);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (outState != null) {
            outState.putParcelableArrayList(RideApplication.VIEWFRAGMENT_MESSAGE_LIST, messageList);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (messageNotificationReceiver == null) {
            messageNotificationReceiver = new MessageNotificationReceiver();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(RideApplication.ARG_MESSAGE_NOTIFICATION);

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageNotificationReceiver, intentFilter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view, container, false);

        recyclerView = view.findViewById(R.id.view_recyclerview);

        recyclerView.setClickable(true);

        recyclerView.isFocusable();

        recyclerView.setFocusableInTouchMode(true);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(viewAdapter);

        smoothScroller = new LinearSmoothScroller(getActivity()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            /*
            @Override
            protected void updateActionForInterimTarget(Action action) {
                action.jumpTo(position);
            }
            */
        };

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();

        display.getSize(size);

        RideLinearLayoutManager layoutManager = new RideLinearLayoutManager(getActivity());

        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        layoutManager.setExtraLayoutSpace(size.y);
        layoutManager.setAutoMeasureEnabled(true);

        recyclerView.setLayoutManager(layoutManager);

        final ItemTouchHelper swipeToDismissHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    final float alpha = RideApplication.ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                if ((messageList != null) && (viewHolder != null)) {
                    final int position = viewHolder.getAdapterPosition();

                    final Message message = messageList.get(position);

                    if ((viewAdapter != null) && (message != null)) {

                        if (direction == ItemTouchHelper.RIGHT) {

                            if (message.getNoid() > 0) {
                                clearNoIdInRideDB(message);

                                message.setNoid(0);
                            }

                            if ((message.getLink() != null) && (message.getLink().length() > 0)) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                                if (preferences != null) {
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt(RideApplication.ARG_LAYOUT_POSITION, viewHolder.getLayoutPosition()).apply();
                                }

                                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                                    try {
                                        startActivityForResult(intent, RideApplication.ACTIVITY_REQUEST_CODE);

                                    } catch (Exception e) {
                                        // e.printStackTrace();
                                    }
                                }

                                Handler handler = new Handler(Looper.getMainLooper());

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (viewAdapter != null) {
                                            viewAdapter.notifyItemChanged(position);
                                        }
                                    }
                                }, 300);

                            } else {
                                viewAdapter.notifyItemChanged(position);
                            }

                        } else {
                            viewAdapter.removeItem(message);

                            messageList.remove(message);

                            delOneFromRideDB(message);

                            onPressed(-1, SUCCESSFULLY_REMOVED);

                            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

                            if (manager != null) {
                                manager.cancelAll();
                            }

                            if (messageList.size() == 0) {

                                if (emptyImage != null) {
                                    AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                    animation.setDuration(1000);
                                    animation.setStartOffset(0);

                                    emptyImage.setVisibility(View.VISIBLE);
                                    emptyImage.startAnimation(animation);
                                }
                            }
                        }
                    }
                }
            }
        });

        swipeToDismissHelper.attachToRecyclerView(recyclerView);

        emptyImage = view.findViewById(R.id.view_emptyImage);

        if (emptyImage != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .circleCrop()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .fallback(R.drawable.icon)
                    .error(R.drawable.icon);

            if ((preferences != null) && preferences.getString(RideApplication.ARG_ICON, "").length() > 0) {
                GlideApp.with(this).load(preferences.getString(RideApplication.ARG_ICON, "https://load.barubox.com/images/noimage.png"))
                        .apply(requestOptions)
                        .into(emptyImage);

                if (preferences.getString(RideApplication.ARG_LINK, "").length() > 0) {

                    emptyImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(preferences.getString(RideApplication.ARG_LINK, "")));

                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

                                try {
                                    startActivity(intent);

                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }
                            }
                        }
                    });
                }

            } else {
                int random = new Random().nextInt(10000);

                if ((random % 2) == 0) {
                    GlideApp.with(this).load(R.drawable.image_1)
                            .apply(requestOptions)
                            .into(emptyImage);

                } else if ((random % 3) == 0) {
                    GlideApp.with(this).load(R.drawable.image_2)
                            .apply(requestOptions)
                            .into(emptyImage);

                } else {
                    GlideApp.with(this).load(R.drawable.icon)
                            .apply(requestOptions)
                            .into(emptyImage);
                }

                emptyImage.setOnClickListener(null);
            }
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if ((preferences != null) && (viewAdapter != null) && (messageList != null) && (messageList.size() > 0)) {
                        View container = getActivity().findViewById(R.id.content_ride);

                        if (container != null) {
                            Snackbar snackbar = Snackbar.make(container, R.string.delete_all_messages, Snackbar.LENGTH_SHORT)
                                    .setAction(R.string.yes, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            RideDBHelper.delAllFromDB(RideApplication.DB_MESSAGES_TABLE);

                                            messageList.clear();

                                            viewAdapter.notifyDataSetChanged();

                                            if (messageList.size() == 0) {

                                                if (emptyImage != null) {
                                                    AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                                                    animation.setDuration(1000);
                                                    animation.setStartOffset(0);

                                                    emptyImage.setVisibility(View.VISIBLE);
                                                    emptyImage.startAnimation(animation);
                                                }
                                            }
                                        }
                                    });

                            snackbar.show();
                        }
                    }
                }
            });
        }

        return view;
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
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.news);
        }

        AppBarLayout appBarLayout = getActivity().findViewById(R.id.appbar);

        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, true);
        }

        if ((messageList == null) || (messageList.size() == 0)) {

            if (emptyImage != null) {
                AlphaAnimation animation = new AlphaAnimation(.0f, 1.f);

                animation.setDuration(1000);
                animation.setStartOffset(0);

                emptyImage.setVisibility(View.VISIBLE);
                emptyImage.startAnimation(animation);
            }

        } else {

            if (recyclerView != null) {

                if ((preferences != null) && !useArguments) {
                    recyclerView.scrollToPosition(preferences.getInt(RideApplication.ARG_LAYOUT_POSITION, 0));

                } else {
                    recyclerView.scrollToPosition(getArguments().getInt(RideApplication.ARG_LAYOUT_POSITION, 0));

                    useArguments = true;
                }
            }
        }

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            manager.cancelAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (emptyImage != null) {
            emptyImage.setVisibility(View.GONE);
        }

        if ((recyclerView != null) && (recyclerView.getLayoutManager() != null)) {
            onPressed(((RideLinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition(), -1);
        }
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

        if (messageNotificationReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageNotificationReceiver);
            messageNotificationReceiver = null;
        }
    }

    public void onPressed(int layoutPosition, int errorCode) {

        if (mViewListener != null) {
            mViewListener.onViewFragment(layoutPosition, errorCode);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnViewListener) {
            mViewListener = (OnViewListener) context;

        } else {
            throw new RuntimeException(context.toString() + " must implement OnViewListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mViewListener = null;
    }

    public interface OnViewListener {
        void onViewFragment(int layoutPosition, int errorCode);
    }

    private void getAllFromRideDB() {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getReadableDatabase();

            if (db != null) {
                int size = messageList.size();

                if (size > 0) {
                    messageList.clear();

                    viewAdapter.notifyItemRangeRemoved(0, size);
                }

                Cursor cursor = db.rawQuery("SELECT * FROM messages ORDER BY _id DESC LIMIT ?", new String[]{Integer.toString(RideApplication.DB_MAX_BACKLOG_ENTRIES)});

                while (cursor.moveToNext()) {
                    Message message = new Message();

                    message.setHead(cursor.getString(1));
                    message.setText(cursor.getString(2));
                    message.setLink(cursor.getString(3));
                    message.setIcon(cursor.getString(4));
                    message.setShot(cursor.getString(5));
                    message.setProd(cursor.getString(6));
                    message.setTime(cursor.getLong(7));
                    message.setNoid(cursor.getInt(8));

                    messageList.add(message);
                }

                cursor.close();
            }
        }
    }

    private void delOldFromRideDB() {
        final int counter = RideDBHelper.getCountFromDB(RideApplication.DB_MESSAGES_TABLE);

        if (counter > RideApplication.DB_MAX_BACKLOG_ENTRIES) {
            final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

            if (rideDBHelper != null) {
                final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

                if (db != null) {
                    db.delete(RideApplication.DB_MESSAGES_TABLE, "_id IN (SELECT _id FROM messages ORDER BY _id ASC LIMIT ?)", new String[]{Integer.toString(counter - RideApplication.DB_MAX_BACKLOG_ENTRIES)});
                }
            }
        }
    }

    private void delOneFromRideDB(Message message) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                db.delete(RideApplication.DB_MESSAGES_TABLE, "time = ?", new String[]{Long.toString(message.getTime())});
            }
        }
    }

    protected void clearNoIdInRideDB(Message message) {
        final RideDBHelper rideDBHelper = RideApplication.getRideDBHelper();

        if (rideDBHelper != null) {
            final SQLiteDatabase db = rideDBHelper.getWritableDatabase();

            if (db != null) {
                ContentValues values = new ContentValues();

                values.put(RideApplication.ARG_NOID, 0);

                db.update(RideApplication.DB_MESSAGES_TABLE, values, "time = ? AND noid = ?", new String[]{Long.toString(message.getTime()), Integer.toString(message.getNoid())});
            }
        }
    }

    private class MessageNotificationReceiver extends BroadcastReceiver {

        public MessageNotificationReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action != null) {

                if (action.equals(RideApplication.ARG_MESSAGE_NOTIFICATION)) {
                    Bundle extras = intent.getExtras();

                    if (extras != null) {
                        Message message = extras.getParcelable(RideApplication.ARG_MESSAGE);

                        if ((viewAdapter != null) && (message != null)) {
                            View view = getView();

                            if (view != null) {
                                emptyImage = view.findViewById(R.id.view_emptyImage);

                                if (emptyImage != null) {
                                    emptyImage.setVisibility(View.GONE);
                                }
                            }

                            messageList.add(0, message);

                            viewAdapter.addItem(message);

                            if ((recyclerView != null) && (viewAdapter.getItemCount() > 0)) {

                                if ((smoothScroller != null) && (((RideLinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > 0)) {
                                    smoothScroller.setTargetPosition(0);

                                    recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);

                                } else {
                                    ((RideLinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RideApplication.ACTIVITY_REQUEST_CODE) {

            if (viewAdapter != null) {
                viewAdapter.notifyDataSetChanged();
            }

            useArguments = false;
        }
    }

}
