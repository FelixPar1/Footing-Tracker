package com.example.footingtrainmap;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {


    private static final String TAG = "LogAppHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RaceDataBase bdd = new RaceDataBase(this);
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = bdd.getReadableDatabase();

            String query = "SELECT nom, temps, distance_totale, vitesse FROM Course";
            cursor = db.rawQuery(query, null);

            ArrayList<String> courseList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    String nom = cursor.getString(0);
                    int temps = cursor.getInt(1);
                    double distance = cursor.getDouble(2);
                    double vitesse = cursor.getDouble(3);

                    courseList.add(nom + ", Temps: " + temps/60000 + " minutes , Distance: " + distance + ", Vitesse: " + vitesse);
                } while (cursor.moveToNext());
            }

            ListView listView = findViewById(R.id.listview1);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
            listView.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "erreur lors du chargement de la base de données");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }


    }

}