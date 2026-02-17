package com.supragyan.grievancems.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

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
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        if (!(haveConnectedWifi || haveConnectedMobile)) {
            //vibrate(300, context);
            Toast.makeText(context, "No Network Connectivity", Toast.LENGTH_SHORT).show();
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
