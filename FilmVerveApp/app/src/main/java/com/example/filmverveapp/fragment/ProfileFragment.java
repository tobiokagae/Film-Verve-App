package com.example.filmverveapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filmverveapp.R;
import com.example.filmverveapp.activity.LoginActivity;
import com.example.filmverveapp.activity.MainActivity;
import com.example.filmverveapp.database.DBHelper;
import com.example.filmverveapp.model.User;

public class ProfileFragment extends Fragment {

    private DBHelper dbHelper;
    private String username;
    private Boolean isLoggedIn;
    private TextView tvName, tvUsername, tvLogout, tvLogin;
    private Button btnDeleteAccount;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DBHelper(getContext());

        tvName = view.findViewById(R.id.tv_name);
        tvUsername = view.findViewById(R.id.tv_username);
        tvLogout = view.findViewById(R.id.tv_logout);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        tvLogin = view.findViewById(R.id.tv_login);

        sharedPreferences = getActivity().getSharedPreferences("data_pref", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isNetworkAvailable()) {
            showNetworkAlertDialog();
            return;
        }

        if (username != null && isLoggedIn) {
            User user = dbHelper.getUser(username);
            if (user != null) {
                tvName.setText(user.getName());
                tvUsername.setText(user.getUsername());
            } else {
                tvName.setText("Name not found");
                tvUsername.setText("Username not found");
            }
            tvLogout.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE);
        } else {
            tvLogin.setVisibility(View.VISIBLE);
            tvLogin.setOnClickListener(v -> navigateToLogin());
        }

        tvLogout.setOnClickListener(v -> showLogoutAlert());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountAlert());
    }

    private void showLogoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void showDeleteAccountAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteUserAccount() {
        boolean isDeleted = dbHelper.deleteUser(username);
        if (isDeleted) {
            Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void navigateToMain(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.putString("username", "");
        editor.apply();
        navigateToMain();
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

}
