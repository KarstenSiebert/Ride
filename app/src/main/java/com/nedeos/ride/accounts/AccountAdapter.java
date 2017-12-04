package com.nedeos.ride.accounts;

import android.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;
import com.nedeos.ride.R;
import com.nedeos.ride.details.DetailPagerFragment;
import com.nedeos.ride.messages.GlideApp;
import com.nedeos.ride.users.User;

import java.util.ArrayList;

/**
 * Created by Karsten on 02.04.2017.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewRowHolder> {

    private ArrayList<User> userList;

    private AccountFragment accountFragment;

    public static class ViewRowHolder extends RecyclerView.ViewHolder {

        ImageView icon;

        public ViewRowHolder(View itemView, int width, final AccountAdapter adapter) {
            super(itemView);

            if (width > 35) {
                itemView.getLayoutParams().width = (width - 36) / 3;
            }

            icon = itemView.findViewById(R.id.account_icon);

            if ((icon != null) && (adapter != null)) {

                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            FragmentManager fragmentManager = adapter.getAccountFragment().getFragmentManager();

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

    public AccountAdapter(AccountFragment accountFragment, ArrayList<User> userList) {
        this.accountFragment = accountFragment;

        this.userList = userList;
    }

    @Override
    public ViewRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_items, parent, false);

        return new ViewRowHolder(v, parent.getWidth(), this);
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
                            .override(100, 94)
                            .dontAnimate()
                            .centerCrop();

                    GlideApp.with(accountFragment).load(user.getIcon())
                            .apply(requestOptions)
                            .into(holder.icon);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != userList ? userList.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(final User item) {

        if ((userList != null) && (item != null) && (userList.indexOf(item) < getItemCount())) {
            userList.add(item);

            if (userList.indexOf(item) == 0) {
                notifyDataSetChanged();

            } else {
                notifyItemInserted(userList.indexOf(item));
            }
        }
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public AccountFragment getAccountFragment() {
        return accountFragment;
    }

}
