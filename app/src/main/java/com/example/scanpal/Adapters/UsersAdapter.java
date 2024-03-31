package com.example.scanpal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;

import java.util.ArrayList;


/**
 * This class is used to display the list of Users in a RecyclerView.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    ArrayList<User> users;
    Context context;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_content, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);
        holder.fullName.setText(user.getFirstName() + " " + user.getLastName());
        Glide.with(context)
                .load(user.getPhoto())
                .into(holder.profilePic);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void addUser(User user) {
        users.add(user);
        notifyItemInserted(users.size() - 1);
    }

    public User getAt(int index) {
        return users.get(index);
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView fullName;
        ImageView profilePic;

        UsersViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.user_full_name);
            profilePic = itemView.findViewById(R.id.user_profile_pic);
        }
    }
}
