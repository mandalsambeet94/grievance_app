package com.supragyan.grievancems.utility;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import com.supragyan.grievancems.ui.LoginActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Util {
    public static HashMap<String,String> fgCodeIdMap;
    public static HashMap<String,String> monoCodeIdMap;

    public static HashMap<String, Integer> getMasterCountIdMap() {
        if(masterCountIdMap == null){
            masterCountIdMap = new HashMap<>();
        }
        return masterCountIdMap;
    }

    public static void setMasterCountIdMap(HashMap<String, Integer> masterCountIdMap) {
        Util.masterCountIdMap = masterCountIdMap;
    }

    public static HashMap<String,Integer> masterCountIdMap;
    public static ArrayList<HashMap<String,String>> pageCount = new ArrayList<>();
    public static HashMap<String, String> getFgCodeIdMap() {
        if(fgCodeIdMap == null){
            fgCodeIdMap = new HashMap<>();
        }
        return fgCodeIdMap;
    }

    public static void setFgCodeIdMap(HashMap<String, String> fgCodeIdMap) {
        Util.fgCodeIdMap = fgCodeIdMap;
    }

    public static HashMap<String, String> getMonoCodeIdMap() {
        if(monoCodeIdMap == null){
            monoCodeIdMap = new HashMap<>();
        }
        return monoCodeIdMap;
    }

    public static void setMonoCodeIdMap(HashMap<String, String> monoCodeIdMap) {
        Util.monoCodeIdMap = monoCodeIdMap;
    }

    public static void clearFgCodeMap() {
        if(fgCodeIdMap != null){
            fgCodeIdMap.clear();
        }
    }
    public static void clearMonoCodeMap() {
        if(monoCodeIdMap != null){
            monoCodeIdMap.clear();
        }
    }

    public static boolean validateFields(EditText editText) {
        if (editText.getText().toString().length() > 0)
            return true;
        else {
            editText.requestFocus();
            editText.setError("This field is required.");
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    public static boolean isNetworkSlow() {
        try {
            long startTime = System.currentTimeMillis();

            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000); // 3 sec timeout
            connection.setReadTimeout(3000);
            connection.setUseCaches(false);
            connection.connect();

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // 👉 Adjust this threshold as needed
            return responseTime > 2000; // >2 sec = slow network

        } catch (Exception e) {
            // If error → treat as slow network
            return true;
        }
    }

    public static void isConnectionSlowAsync(InternetCheckCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean isSlow;

            try {
                long startTime = System.currentTimeMillis();

                URL url = new URL("https://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                isSlow = duration > 2000;
            } catch (Exception e) {
                isSlow = true;
            }

            boolean finalIsSlow = isSlow;

            // Return result on main thread
            handler.post(() -> callback.onResult(finalIsSlow));
        });
    }

    public static void logoutAll(Context context) {
        SharedPreferenceClass sharedPreferenceClass= new SharedPreferenceClass(context);
        sharedPreferenceClass.clearData();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
