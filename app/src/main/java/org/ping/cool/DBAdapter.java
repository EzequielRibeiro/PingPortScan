package org.ping.cool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class DBAdapter {

    static final String KEY_ID = "id";
    static final String KEY_URL = "code";
    static final String KEY_COMMENTS = "comments";
    static final String DATABASE_NAME = "pingdatabase";
    static final String DATABASE_TABLE_URL = "url";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE_FAVORITE = "CREATE TABLE IF NOT EXISTS "+ DATABASE_TABLE_URL +"(" +
             KEY_ID + " integer primary key autoincrement," +
             KEY_URL + " text not null unique ON CONFLICT ABORT," +
             KEY_COMMENTS +  " text);";

    final Context context;
    DataBaseHelper dataBaseHelper;
    SQLiteDatabase db;

    public DBAdapter(Context cont) {
        context = cont;
        dataBaseHelper = new DataBaseHelper(context);
        db = dataBaseHelper.getWritableDatabase();
    }

    public void close() {
        dataBaseHelper.close();
        db.close();
    }

    //insert
    public long insertUrl(String url, String comments) throws SQLException {

        if(getCount(url) > 0){
            return 1;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_URL, url);
        initialValues.put(KEY_COMMENTS, comments);
        long i = db.insert(DATABASE_TABLE_URL, null, initialValues);
        return i;
    }


    public boolean deleteUrl(long id) throws SQLException {

        return db.delete(DATABASE_TABLE_URL, "id=" + id, null) > 0;

    }


    public boolean deleteAllUrl() throws SQLException {

        return db.delete(DATABASE_TABLE_URL, null, null) > 0;

    }

    //retriever all values from database
    public List<UrlHistoric> getAllValuesGlyphs() throws SQLException {

        List<UrlHistoric> urlHistoricArrayList = new LinkedList<UrlHistoric>();

        Cursor cursor =  db.query(DATABASE_TABLE_URL,
                new String[]{"id", KEY_URL, KEY_COMMENTS}, null, null, null, null, "id DESC");

        UrlHistoric urlHistoric;

        if (cursor.moveToFirst()) {
            do {
                urlHistoric = new UrlHistoric();
                urlHistoric.setId(cursor.getInt(0));
                urlHistoric.setText(cursor.getString(1));
                urlHistoric.setComment(cursor.getString(2));
                urlHistoricArrayList.add(urlHistoric);
            } while (cursor.moveToNext());
        }
        return urlHistoricArrayList;

    }

    public boolean updateGlyphs(int id, String code, String commenst) throws SQLException {

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_URL, code);
        contentValues.put(KEY_COMMENTS, commenst);

        return db.update(DATABASE_TABLE_URL, contentValues, "id=" + id, null) > 0;

    }

    public int getCount(String url){
        Cursor cursor;
        int count = 0;

        cursor = db.rawQuery("SELECT "+ KEY_URL +" FROM "+DATABASE_TABLE_URL+ " WHERE "+KEY_URL+" = ?", new String[] {url});
        if(cursor.moveToFirst())
            count = cursor.getCount();
        cursor.close();

        return count;


    }

    private static class DataBaseHelper extends SQLiteOpenHelper {


        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            try {
                sqLiteDatabase.execSQL(DATABASE_CREATE_FAVORITE);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_URL);
            onCreate(sqLiteDatabase);
        }

    }


}
