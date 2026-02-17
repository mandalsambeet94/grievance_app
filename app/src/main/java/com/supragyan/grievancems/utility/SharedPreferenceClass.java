package com.supragyan.grievancems.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {

	private static final String USER_PREFS = "GrievanceMS";
	private static final String USER_PREFS_NEW = "GrievanceMSNEW";
	private SharedPreferences appSharedPrefs;
	private SharedPreferences appSharedPrefsNew;
	private SharedPreferences.Editor prefsEditor;
	private SharedPreferences.Editor prefsEditorNew;

	// private String user_name = "user_name_prefs";
	// String user_id = "user_id_prefs";

	public SharedPreferenceClass(Context context) {
		this.appSharedPrefs = context.getSharedPreferences(USER_PREFS,Activity.MODE_PRIVATE);
		this.prefsEditor = appSharedPrefs.edit();

		this.appSharedPrefsNew = context.getSharedPreferences(USER_PREFS_NEW,Activity.MODE_PRIVATE);
		this.prefsEditorNew = appSharedPrefsNew.edit();
	}

	//get value
	public int getValue_int(String intKeyValue) {
		return appSharedPrefs.getInt(intKeyValue, 1);
	}

	public String getValue_string(String stringKeyValue) {
		return appSharedPrefs.getString(stringKeyValue, "false");
	}

	public String getValue_string_new(String stringKeyValue) {
		return appSharedPrefsNew.getString(stringKeyValue, "false");
	}

	public Boolean getValue_boolean(String stringKeyValue) {
		return appSharedPrefs.getBoolean(stringKeyValue, false);
	}

	//setvalue
	
	public void setValue_int(String intKeyValue, int _intValue) {

		prefsEditor.putInt(intKeyValue, _intValue).commit();
	}

	public void setValue_string(String stringKeyValue, String _stringValue) {

		prefsEditor.putString(stringKeyValue, _stringValue).commit();

	}

	public void setValue_string_new(String stringKeyValue, String _stringValue) {

		prefsEditorNew.putString(stringKeyValue, _stringValue).commit();

	}
	
	public void setValue_boolean(String stringKeyValue, Boolean _bool) {

		prefsEditor.putBoolean(stringKeyValue, _bool).commit();

	}

	public void setValue_int(String intKeyValue) {

		prefsEditor.putInt(intKeyValue, 0).commit();
	}

	public void clearData() {
		prefsEditor.clear().commit();

	}
}