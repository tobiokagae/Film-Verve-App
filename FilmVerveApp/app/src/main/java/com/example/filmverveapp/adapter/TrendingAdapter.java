package com.example.filmverveapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.filmverveapp.R;
import com.example.filmverveapp.activity.DetailActivity;
import com.example.filmverveapp.database.DBHelper;
import com.example.filmverveapp.model.Movie;

import java.text.BreakIterator;
import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.ViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public TrendingAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_trending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        Glide.with(context)
                .load(movie.getPoster())
                .placeholder(R.color.bluegray) // placeholder image
                .into(holder.ivPoster);

        holder.tvAge.setText(movie.getAge() ? "18+" : "PG");

        holder.cardView.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("data_pref", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", null);
            if (username != null) {
                DBHelper databaseHelper = new DBHelper(context);
                int userId = databaseHelper.getUserIdByUsername(username);

                if (userId != -1) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("movie_id", movie.getId());
                    context.startActivity(intent);
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Login Required")
                            .setMessage("You need to log in to view the movie details.")
                            .setPositiveButton("Ok", null)
                            .show();
                }
            } else {
                new AlertDialog.Builder(context)
                        .setTitle("Login Required")
                        .setMessage("You need to log in to view the movie details.")
                        .setPositiveButton("Ok", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPoster;
        TextView tvAge;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvAge = itemView.findViewById(R.id.tv_age);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }
}
