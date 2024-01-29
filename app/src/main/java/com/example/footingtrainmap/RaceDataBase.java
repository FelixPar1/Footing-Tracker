package com.example.footingtrainmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RaceDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ma_base_de_donnees";
    private static final int DATABASE_VERSION = 1;

    // Table Course
    private static final String TABLE_COURSE = "Course";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOM = "nom";
    private static final String COLUMN_TEMPS = "temps";
    private static final String COLUMN_DISTANCE_TOTALE = "distance_totale";
    private static final String COLUMN_VITESSE = "vitesse";

    // Table Points
    private static final String TABLE_POINTS = "Points";
    private static final String COLUMN_ID_POINTS = "id_points";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_HEURE = "heure";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_COURSE_ID = "course_id";

    public RaceDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table Course
        String CREATE_TABLE_COURSE = "CREATE TABLE " + TABLE_COURSE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOM + " TEXT,"
                + COLUMN_TEMPS + " INTEGER,"
                + COLUMN_DISTANCE_TOTALE + " REAL,"
                + COLUMN_VITESSE + " REAL" + ")";
        db.execSQL(CREATE_TABLE_COURSE);

        // Création de la table Points
        String CREATE_TABLE_POINTS = "CREATE TABLE " + TABLE_POINTS + "("
                + COLUMN_ID_POINTS + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_HEURE + " REAL,"
                + COLUMN_DISTANCE + " REAL,"
                + COLUMN_COURSE_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + TABLE_COURSE + "(" + COLUMN_ID + ")" + ")";
        db.execSQL(CREATE_TABLE_POINTS);

        insertDefaultCourse(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        onCreate(db);
    }

    //just for tests
    private void insertDefaultCourse(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("nom", "Course Test");
        values.put("temps", 3600);
        values.put("distance_totale", 10.0);
        values.put("vitesse", 10.0);

        db.insert("Course", null, values);
    }

}
