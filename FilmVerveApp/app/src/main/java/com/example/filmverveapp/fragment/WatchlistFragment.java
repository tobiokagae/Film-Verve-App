package com.example.filmverveapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filmverveapp.R;
import com.example.filmverveapp.adapter.ListAdapter;
import com.example.filmverveapp.database.DBHelper;
import com.example.filmverveapp.model.Movie;
import com.example.filmverveapp.api.APIConfig;
import com.example.filmverveapp.api.APIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchlistFragment extends Fragment {

    private ListAdapter listAdapter;
    private List<Movie> movieList;
    private DBHelper dbHelper;
    private APIService apiService;
    private static final String key = "456d859f86b1222d4eaa386b66f700ba";
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;
    private TextView tvNoData;
    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DBHelper(getContext());
        movieList = new ArrayList<>();
        apiService = APIConfig.getAPIService();
        listAdapter = new ListAdapter(getContext(), movieList);

        recyclerView = view.findViewById(R.id.rv_watchlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(listAdapter);

        tvNoData = view.findViewById(R.id.tv_no_data);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void loadWatchlistData() {
        sharedPreferences = requireContext().getSharedPreferences("data_pref", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (!isNetworkAvailable()) {
            showNetworkAlertDialog();
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        if (username == null) {
            showLoginAlertDialog();
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        int userId = dbHelper.getUserIdByUsername(username);
        if (userId == -1) {
            showLoginAlertDialog();
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        Cursor cursor = dbHelper.getWatchlistData(userId);
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) cursor.close();
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        tvNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        do {
            @SuppressLint("Range") int movieId = cursor.getInt(cursor.getColumnIndex(dbHelper.getColumnMovieId()));
            fetchMovieDetails(movieId);
        } while (cursor.moveToNext());

        cursor.close();
    }


    private void fetchMovieDetails(int movieId) {
        apiService.getMovieDetails(movieId, key).enqueue(new Callback<Movie>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    movieList.add(movie);
                    listAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to get movie details", Toast.LENGTH_SHORT).show();
                }

                if (movieList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showLoginAlertDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Login Required")
                .setMessage("You need to log in to view or make your own watchlist.")
                .setPositiveButton("Ok", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNetworkAlertDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Network Connection")
                .setMessage("You need an active network connection to view the app. Please check your network settings.")
                .setPositiveButton("Settings", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    public void onResume() {
        super.onResume();
        loadWatchlistData();
    }


}
