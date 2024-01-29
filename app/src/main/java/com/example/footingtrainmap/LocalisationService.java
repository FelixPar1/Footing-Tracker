package com.example.footingtrainmap;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class LocalisationService extends Service implements LocationListener {
    private BroadcastReceiver locationReceiver;
    private String TAG = "LogAppLocalisationService";
    private LocationManager mlocationManager;
    private String provider;
    private static final long MIN_TIME_BW_UPDATES = (long) 0.00000001;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = (float) 0.00000001;
    private double latitude = 0;
    private double longitude = 0;
    private int compteur = 0;
    private boolean started = true;
    private long lastLocationChangedTime = 0;
    private ArrayList<GeoPointHorodate> current_points;
    private ArrayList<ArrayList<GeoPointHorodate>> points;

    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 12345678;

    @Override
    public void onCreate() {
        super.onCreate();

        points = new ArrayList<>();
        points.add( new ArrayList<>());
        current_points = points.get(0);


        Log.i(TAG, "onCreate");
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        vibrer(35000);
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    // réception des données du broadcast
                    double latitude = intent.getDoubleExtra("latitude", 0);
                    double longitude = intent.getDoubleExtra("longitude", 0);
                    int compteur = intent.getIntExtra("test", 0);
                    //Log.d(TAG, "receiveLoc ;   Latitude : " + latitude + ", Longitude : " + longitude + ", compteur:" + compteur);
                } catch (Exception e) {
                    Log.e(TAG, "erreur de réception du BroadcastReceiver (loc)");
                }
            }
        };


        // Enregistrement du BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOCATION_UPDATE");
        registerReceiver(locationReceiver, intentFilter);


        // Initialisation de la demande de mises à jour de position
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        //lancement de la notification de premier plan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Running...")
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }


        // tâche asynchrone qui envoie des données à l'activité
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {


                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction("LOCATION_UPDATE");
                        broadcastIntent.putExtra("latitude", latitude);
                        broadcastIntent.putExtra("longitude", longitude);
                        broadcastIntent.putExtra("test", compteur);
                        broadcastIntent.putExtra("points", current_points);
                        broadcastIntent.putExtra("lastLocationChangedTime", lastLocationChangedTime);
                        sendBroadcast(broadcastIntent);

                        compteur++;
                    } catch (Exception e) {
                        Log.e(TAG, "envoie impossible");
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(RaceActivity.instance, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permission2 = ContextCompat.checkSelfPermission(RaceActivity.instance, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(RaceActivity.instance, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        // Check for permissions
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "Requesting Permissions");

            ActivityCompat.requestPermissions(RaceActivity.instance,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 565);
        }

        if (permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "Requesting Permissions");

            ActivityCompat.requestPermissions(RaceActivity.instance,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 567);
        }

        if (permission3 != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "Requesting Permissions");

            ActivityCompat.requestPermissions(RaceActivity.instance,
                    new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 566);
        }
        else Log.d("permission", "Permissions Already Granted");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        // libération des ressources
        unregisterReceiver(locationReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Log.d(TAG, "nouvelle localisation détectée   "+ latitude + "  " + longitude + "    acc : " + location.getAccuracy());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        manageNewLocation(location);
    }


    private void manageNewLocation(Location location) {
        if (started) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            GeoPointHorodate newPoint = new GeoPointHorodate(latitude, longitude, SystemClock.elapsedRealtime(), 0);
            if (current_points.size()>0) {
                GeoPointHorodate lastPoint = current_points.get(current_points.size()-1);
                float[] results = new float[1];
                Location.distanceBetween(newPoint.getLatitudeE6() / 1E6, newPoint.getLongitudeE6() / 1E6,
                        lastPoint.getLatitudeE6() / 1E6, lastPoint.getLongitudeE6() / 1E6, results);
                newPoint.setDistance(lastPoint.getDistance() + (long) results[0]);
                //Log.d(TAG, "distance du dernier point: " + newPoint.getDistance());
                //tous les kilomètres, vibrations du téléphone pour indiquer à l'utilisateur sa vitesse
                //TODO : pouvoir personnaliser ces paramètres dans l'interface
                if (newPoint.getDistance()/1000 > lastPoint.getDistance()/1000) {
                    long temps = newPoint.getHeure() - current_points.get(0).getHeure();
                    long vitesseCible = 12000;
                    long tempsCible = newPoint.getDistance() / vitesseCible;
                    long delai = temps - tempsCible; //négatif si en retard
                    vibrer(delai);
                    Log.d(TAG, "km n° " + newPoint.getDistance()/1000 + "   delai (s): " + delai/1000);

                }
            }
            if (location.getAccuracy() <= 5) {
                lastLocationChangedTime = SystemClock.elapsedRealtime();
                ajouter_points(newPoint);
                //Log.d(TAG, "nouvelle coordonnée détectée  : " + latitude + "  " + longitude);
            }
        }
    }

    /*ajoute un points à la liste de points courant
    la liste de points courant ne peut pas dépasser 1500 pour ne pas avoir trop de données à envoyer en broadcast
    si elle dépasse 1500, on passe sur une nouvelle liste
     */
    private void ajouter_points(GeoPointHorodate newPoint) {
        if (current_points.size() > 1500) {
            points.add(new ArrayList<>());
            current_points = points.get(points.size() - 1);
        }
        current_points.add(newPoint);
    }


    /*fais vibrer le téléphone d'un nombre égal au nombre de dizaine de secondes en retard ou en avance
    après une première vibration dont la longueur indique si avance ou retard*/
    private void vibrer(long delai) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern;
        long nb = Math.abs(delai / 10000) + 1; // nb de dizaines de secondes
        pattern = new long[(int) (nb * 2 + 3)];
        pattern[0] = 0;
        if (delai<0) {
            pattern[1] = 1000;
        } else {
            pattern[1] = 300;
        }
        pattern[2] = 1000;

        for (int i = 1; i <= nb; i++) {
            pattern[i * 2 + 1] = 500; // Vibration de 0.5 seconde
            pattern[i * 2 + 2] = 100; // Pause de 0.1 seconde
        }
        vibrator.vibrate(pattern, -1);

    }
}
