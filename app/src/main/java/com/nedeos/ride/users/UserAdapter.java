package com.nedeos.ride.users;

import android.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.details.DetailImageView;
import com.nedeos.ride.details.DetailPagerFragment;
import com.nedeos.ride.messages.GlideApp;

import java.util.ArrayList;

/**
 * Created by Karsten on 02.04.2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewRowHolder> {

    private ArrayList<User> userList;

    private UserFragment userFragment;

    public static class ViewRowHolder extends RecyclerView.ViewHolder {

        DetailImageView icon;

        public ViewRowHolder(View itemView, final UserAdapter adapter) {
            super(itemView);

            icon = itemView.findViewById(R.id.user_icon);

            if ((icon != null) && (adapter != null)) {

                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            FragmentManager fragmentManager = adapter.getUserFragment().getFragmentManager();

                            if (fragmentManager != null) {
                                fragmentManager.beginTransaction()
                                        .replace(R.id.content_ride, DetailPagerFragment.newInstance(adapter.getUserList(), position), "DetailPagerFragment")
                                        .addToBackStack("DetailPagerFragment")
                                        .commit();
                            }
                        }
                    }
                });
            }
        }
    }

    public UserAdapter(UserFragment userFragment, ArrayList<User> userList) {
        this.userFragment = userFragment;

        this.userList = userList;
    }

    @Override
    public ViewRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_items, parent, false);

        return new ViewRowHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewRowHolder holder, int position) {
        int safePosition = holder.getAdapterPosition();

        if ((safePosition != RecyclerView.NO_POSITION) && (userList != null) && (userList.size() > safePosition)) {
            final User user = userList.get(safePosition);

            holder.icon.setVisibility(View.VISIBLE);

            if (user != null) {

                if ((user.getIcon() != null) && (user.getIcon().length() > 0)) {

                    RequestOptions requestOptions = new RequestOptions()
                            .dontAnimate()
                            .centerCrop();

                    GlideApp.with(userFragment).load(user.getIcon())
                            .apply(requestOptions)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                            .into(holder.icon);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != userList ? userList.size() : 0);
    }

    public void addItem(final User item) {

        if ((userList != null) && (item != null)) {
            userList.add(item);

            if (userList.indexOf(item) == 0) {
                notifyDataSetChanged();

            } else {
                notifyItemInserted(userList.indexOf(item));
            }
        }
    }

    public void removeItem(final User item) {

        if ((userList != null) && (item != null)) {
            notifyItemRemoved(userList.indexOf(item));

            userList.remove(item);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public UserFragment getUserFragment() {
        return userFragment;
    }

}
