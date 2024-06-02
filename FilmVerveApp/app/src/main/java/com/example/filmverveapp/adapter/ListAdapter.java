package com.example.filmverveapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.util.List;
import java.util.Locale;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private Context context;
    private List<Movie> movieList;

    public ListAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle,tvRating;
        private ImageView posterImageView;
        private CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvRating = itemView.findViewById(R.id.tv_rating);
            posterImageView = itemView.findViewById(R.id.iv_poster);
            cardView = itemView.findViewById(R.id.cardview);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Movie movie) {
            tvTitle.setText(movie.getTitle());
            String ratingText = String.valueOf(movie.getRating());
            if (ratingText.length() > 3) {
                ratingText = ratingText.substring(0, 3);
            }
            tvRating.setText(ratingText);
            Glide.with(context).load("https://image.tmdb.org/t/p/w500"
                    + movie.getPoster()).into(posterImageView);

            cardView.setOnClickListener(v -> {
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
    }

}
