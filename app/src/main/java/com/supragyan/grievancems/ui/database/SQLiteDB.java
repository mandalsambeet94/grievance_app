package com.supragyan.grievancems.ui.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


public class SQLiteDB {
    private static final String DATABASE_NAME = "SQLiteDB.db";
    private static final int DATABASE_VERSION = 1;
    private final Context context;
    private DatabaseHelper DBHelper;
    private static final String TABLE_CREATE_GRIEVANCE = "table_create_grievance";

    static final String BLOCK = "block";
    static final String GP = "gp";
    static final String VILLAGE = "village";
    static final String ADDRESS = "address";
    static final String WARD_NO = "ward_no";
    static final String NAME = "name";
    static final String FATHER_NAME = "father_name";
    static final String CONTACT = "contact";
    static final String TOPIC = "topic";
    static final String GRIEVANCE_MATTER = "grievance_matter";
    static final String REMARK = "remark";
    static final String PHOTOS = "photos";
    static final String GRIEVANCE_ID = "grievance_id";
    static final String UPLOAD_ID = "upload_id";
    static final String USERID = "userid";
    static final String OFFLINEID = "offlineid";

    public SQLiteDB(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                String CREATE_GRIEVANCE = "CREATE TABLE IF NOT EXISTS " + TABLE_CREATE_GRIEVANCE
                        + "( id  INTEGER PRIMARY KEY AUTOINCREMENT ,"
                        + OFFLINEID + " TEXT ,"
                        + USERID + " TEXT ,"
                        + BLOCK + " TEXT ,"
                        + GP + " TEXT ,"
                        + VILLAGE + " TEXT ,"
                        + ADDRESS + " TEXT ,"
                        + WARD_NO + " TEXT ,"
                        + NAME + " TEXT ,"
                        + FATHER_NAME + " TEXT ,"
                        + CONTACT + " TEXT ,"
                        + TOPIC + " TEXT ,"
                        + GRIEVANCE_MATTER + " TEXT ,"
                        + REMARK + " TEXT ,"
                        + PHOTOS + " TEXT ,"
                        + GRIEVANCE_ID + " TEXT ,"
                        + UPLOAD_ID + " TEXT )";

                db.execSQL(CREATE_GRIEVANCE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREATE_GRIEVANCE);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            throw new SQLiteException("Can't downgrade database from version " +
                    oldVersion + " to " + newVersion);
        }
    }

    public SQLiteDB open() throws SQLException {
            DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {

        DBHelper.close();
    }

    public void addGrievanceData(GrievanceModel model){
        try{
            SQLiteDatabase db = DBHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(OFFLINEID,model.getOfflineID());
            values.put(USERID,model.getUserID());
            values.put(BLOCK,model.getBlock());
            values.put(GP,model.getGp());
            values.put(VILLAGE,model.getVillage());
            values.put(ADDRESS,model.getAddress());
            values.put(WARD_NO,model.getWardNo());
            values.put(NAME,model.getName());
            values.put(FATHER_NAME,model.getFatherName());
            values.put(CONTACT,model.getContact());
            values.put(TOPIC,model.getTopic());
            values.put(GRIEVANCE_MATTER,model.getGrievanceMatter());
            values.put(REMARK,model.getRemark());
            values.put(PHOTOS,model.getPhotos());
            values.put(GRIEVANCE_ID,model.getGrievanceID());
            values.put(UPLOAD_ID,model.getUploadID());
            db.insert(TABLE_CREATE_GRIEVANCE, null, values);
            db.close();
            Log.d("TABLE_CREATE_GRIEVANCE", "Data inserted");
        }catch (Exception e){
            Log.d("Exception", String.valueOf(e));
        }
    }

    // get all offline sync data
    public ArrayList<GrievanceModel> getAllGrievanceData(String userId) {
        ArrayList<GrievanceModel> contactList = new ArrayList<GrievanceModel>();
        // Select All Query
        String selectQuery;
        selectQuery = "SELECT  * FROM " + TABLE_CREATE_GRIEVANCE + " WHERE " + USERID + " = ?";;

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});
        if (cursor.moveToFirst()) {
            try {
                do {
                    GrievanceModel contact = new GrievanceModel();
                    contact.setOfflineID(cursor.getString(cursor.getColumnIndexOrThrow(OFFLINEID)));
                    contact.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(USERID)));
                    contact.setBlock(cursor.getString(cursor.getColumnIndexOrThrow(BLOCK)));
                    contact.setGp(cursor.getString(cursor.getColumnIndexOrThrow(GP)));
                    contact.setVillage(cursor.getString(cursor.getColumnIndexOrThrow(VILLAGE)));
                    contact.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)));
                    contact.setWardNo(cursor.getString(cursor.getColumnIndexOrThrow(WARD_NO)));
                    contact.setName(cursor.getString(cursor.getColumnIndexOrThrow(NAME)));
                    contact.setFatherName(cursor.getString(cursor.getColumnIndexOrThrow(FATHER_NAME)));
                    contact.setContact(cursor.getString(cursor.getColumnIndexOrThrow(CONTACT)));
                    contact.setTopic(cursor.getString(cursor.getColumnIndexOrThrow(TOPIC)));
                    contact.setGrievanceMatter(cursor.getString(cursor.getColumnIndexOrThrow(GRIEVANCE_MATTER)));
                    contact.setRemark(cursor.getString(cursor.getColumnIndexOrThrow(REMARK)));
                    contact.setPhotos(cursor.getString(cursor.getColumnIndexOrThrow(PHOTOS)));
                    contact.setGrievanceID(cursor.getString(cursor.getColumnIndexOrThrow(GRIEVANCE_ID)));
                    contact.setUploadID(cursor.getString(cursor.getColumnIndexOrThrow(UPLOAD_ID)));
                    // Adding contact to list
                    contactList.add(contact);
                    //Log.v("NAME", "DATABASE" + cursor.getString(4));
                } while (cursor.moveToNext());
            } catch (Exception e) {
                Log.v("SUPRAGYAN", "Exception");
            }
        }
        cursor.close();
        db.close();
        // return contact list
        return contactList;
    }

    public GrievanceModel getGrievanceByOfflineId(String offlineId) {

        GrievanceModel grievance = null;

        String selectQuery =
                "SELECT * FROM " + TABLE_CREATE_GRIEVANCE +
                        " WHERE " + OFFLINEID + " = ? LIMIT 1";

        SQLiteDatabase db = DBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{offlineId});

        if (cursor != null && cursor.moveToFirst()) {
            try {
                grievance = new GrievanceModel();
                grievance.setOfflineID(cursor.getString(cursor.getColumnIndexOrThrow(OFFLINEID)));
                grievance.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(USERID)));
                grievance.setBlock(cursor.getString(cursor.getColumnIndexOrThrow(BLOCK)));
                grievance.setGp(cursor.getString(cursor.getColumnIndexOrThrow(GP)));
                grievance.setVillage(cursor.getString(cursor.getColumnIndexOrThrow(VILLAGE)));
                grievance.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)));
                grievance.setWardNo(cursor.getString(cursor.getColumnIndexOrThrow(WARD_NO)));
                grievance.setName(cursor.getString(cursor.getColumnIndexOrThrow(NAME)));
                grievance.setFatherName(cursor.getString(cursor.getColumnIndexOrThrow(FATHER_NAME)));
                grievance.setContact(cursor.getString(cursor.getColumnIndexOrThrow(CONTACT)));
                grievance.setTopic(cursor.getString(cursor.getColumnIndexOrThrow(TOPIC)));
                grievance.setGrievanceMatter(cursor.getString(cursor.getColumnIndexOrThrow(GRIEVANCE_MATTER)));
                grievance.setRemark(cursor.getString(cursor.getColumnIndexOrThrow(REMARK)));
                grievance.setPhotos(cursor.getString(cursor.getColumnIndexOrThrow(PHOTOS)));
                grievance.setGrievanceID(cursor.getString(cursor.getColumnIndexOrThrow(GRIEVANCE_ID)));
                grievance.setUploadID(cursor.getString(cursor.getColumnIndexOrThrow(UPLOAD_ID)));
            } catch (Exception e) {
                Log.e("SUPRAGYAN", "Error fetching grievance", e);
            }
        }

        if (cursor != null) cursor.close();
        db.close();

        return grievance; // may be null if not found
    }

    public void emptyGrievanceTable() {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CREATE_GRIEVANCE);
        db.close();
    }


    public boolean deleteRow(String codeToMatch) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();

        // Define the WHERE clause
        String whereClause = "offlineid=?";

        // Use a single argument for the WHERE clause
        String[] whereArgs = new String[]{codeToMatch};

        // Execute the delete query
        int result;
        result = db.delete(TABLE_CREATE_GRIEVANCE, whereClause, whereArgs);

        db.close(); // Close the database after use

        // Return true if rows were deleted, otherwise false
        return result > 0;
    }
}
