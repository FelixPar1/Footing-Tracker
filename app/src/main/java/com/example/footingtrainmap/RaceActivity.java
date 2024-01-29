package com.example.footingtrainmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RaceActivity extends AppCompatActivity{

    private static final String TAG = "LogAppRaceActivity";
    private static final long MIN_TIME_BW_UPDATES = (long) 0.00000001;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = (float) 0.00000001;
    private MapView map;
    private Button startButton;
    private Button stopButton;
    private Button enregistrementButton;
    private TextView timerTextView;
    private TextView distanceTextView;
    private TextView averageSpeedTextView;
    private TextView currentSpeedTextView;
    private IMapController mapController;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION_CODE = 1;
    private MyLocationNewOverlay locationOverlay;
    private ArrayList<GeoPointHorodate> pointshorodates;
    private ArrayList<GeoPoint> points;
    private Calendar calendar = Calendar.getInstance();
    private Polyline line;
    private double distanceTotale;
    private double averageSpeed = 0;
    private double currentSpeed = 0;
    private long startTime = 0;
    private long stopTime;
    private long pauseTime = 0;
    private long currentTime = (long) 0.001;//pas à 0 pour éviter la division par zéro lors du calcul de vitesse
    private long lastLocationChangedTime = 0;
    private boolean started = false; //si on est en course ou pas
    private boolean is_registered = false; //si la course est enregistrée
    private Handler handler = new Handler();
    private BroadcastReceiver locationReceiver;
    public static RaceActivity instance;
    private Trip trip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_race);
        Log.d(TAG, "on entre dans raceactivity!");

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));


        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stopButton);
        enregistrementButton = findViewById(R.id.enregistrementButton);
        distanceTextView = findViewById(R.id.distanceTextView);
        averageSpeedTextView = findViewById(R.id.averageSpeed);
        currentSpeedTextView = findViewById(R.id.currentSpeed);
        //accuracyTextView = findViewById(R.id.accuracyTextView);
        timerTextView = findViewById(R.id.timerTextView);
        map = findViewById(R.id.map_view);


        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        //point sur lequel on centre la carte
        GeoPoint startpoint;
        startpoint = new GeoPoint(50.0, 3.0); //TODO: centrer par défaut sur l'utilisateur, ajouter un bouton pour recentrer
        mapController = map.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(startpoint);


        //dessin de la ligne du trajet:
        points = new ArrayList<>();
        pointshorodates = new ArrayList<>();
        line = new Polyline();
        line.setColor(Color.BLUE);
        line.setWidth(5f);
        line.setPoints(points);
        map.getOverlayManager().add(line);


        locationOverlay = new MyLocationNewOverlay(map);
        map.getOverlays().add(locationOverlay);
        locationOverlay.enableMyLocation();
        startpoint = locationOverlay.getMyLocation();
        Log.d(TAG, "startpoint : " + String.valueOf(startpoint));
        mapController.setCenter(startpoint);


        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    // Récupérer les données reçues du broadcast
                    double latitude = intent.getDoubleExtra("latitude", 0);
                    double longitude = intent.getDoubleExtra("longitude", 0);
                    pointshorodates = intent.getParcelableArrayListExtra("points");
                    lastLocationChangedTime = intent.getLongExtra("lastLocationChangedTime", 0);
                    int compteur = intent.getIntExtra("test", 0);
                    updateTrip();
                    //Log.d(TAG, "receiveRace;   Latitude : " + latitude + ", Longitude : " + longitude + ",  compteur:" + compteur + ",   nbPoints : " + pointshorodates.size());

                } catch (Exception e) {
                    Log.e(TAG, "erreur de réception du BroadcastReceiver (rac)");
                }
            }
        };


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
            }
        });

        enregistrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_registered) register_race();
            }
        });
    }



    private void startTracking() {
        Log.d(TAG, "on a commencé la course");
        started = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        enregistrementButton.setEnabled(false);

        //Start foreground service that will schedule the Jobs.
        ContextCompat.startForegroundService(this, new Intent(this, LocalisationService.class));

        //lancement du chrono
        if (startTime <= 0) {
            startTime = SystemClock.elapsedRealtime();
        } else {
            pauseTime += SystemClock.elapsedRealtime() - stopTime;
        }
        handler.postDelayed(timerRunnable, 0);
        timerTextView.setEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trip = new Trip(LocalDate.now(), new Date());
        }
    }


    private void stopTracking() {
        started = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        enregistrementButton.setEnabled(true);
        stopTime = SystemClock.elapsedRealtime();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //arrêt du chrono
        handler.removeCallbacks(timerRunnable);
        timerTextView.setEnabled(false);
        long elapsedTime = System.currentTimeMillis() - startTime;

        trip.setPoints(pointshorodates);
        trip.setDistance(distanceTotale);
        trip.setVitesse((distanceTotale/1000) /  ((stopTime- startTime - pauseTime+1)/3600000));
        trip.setDuration( stopTime- startTime - pauseTime);
        trip.setHeureDebut(new Date(startTime));
        trip.setHeureFin(new Date(stopTime));
    }

    private void register_race() {
        is_registered = true;
        Log.d(TAG, "enregistrement dans la base de données");
        RaceDataBase bdd = new RaceDataBase(this);

        SQLiteDatabase db = bdd.getWritableDatabase();

        try {
            db.beginTransaction();

            // Création d'un objet ContentValues pour stocker les valeurs à insérer dans la table "Course"
            ContentValues values = new ContentValues();
            values.put("nom", trip.getNom());
            values.put("temps", trip.getDuration());
            values.put("distance_totale", trip.getDistance());
            values.put("vitesse", trip.getVitesse());
            long courseId = db.insert("Course", null, values);
            if (courseId == -1) {
                throw new Exception("Erreur lors de l'insertion de la course");
            }
            // Insertion des points associés à la course dans la table "Points"
            for (GeoPointHorodate point : pointshorodates) {
                ContentValues pointValues = new ContentValues();
                pointValues.put("latitude", point.getLatitude());
                pointValues.put("longitude", point.getLongitude());
                pointValues.put("heure", point.getHeure());
                pointValues.put("course_id", courseId);
                db.insert("Points", null, pointValues);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fermeture de la base de données
            db.close();
            Log.d(TAG, "donnees enregistrees");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        line.setPoints(points);
        Log.d(TAG, "on rouvre l'appli...");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOCATION_UPDATE");
        registerReceiver(locationReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //pour le chrono
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            currentTime = SystemClock.elapsedRealtime() - startTime - pauseTime;
            int heures = (int) (currentTime / 3600000);
            int minutes = (int) ((currentTime % 3600000) / 60000);
            int seconds = (int) ((currentTime % 60000) / 1000);
            timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", heures, minutes, seconds));
            handler.postDelayed(this, 0);

            averageSpeed = 3600 * distanceTotale / currentTime;
            averageSpeedTextView.setText(String.valueOf(Math.round(averageSpeed * 10.0) / 10.0) + " km/h");

            currentSpeed = calculCurrentSpeed(pointshorodates);
            currentSpeedTextView.setText(String.valueOf(Math.round(currentSpeed * 10.0) / 10.0) + " km/h");
        }
    };

    //calcule la distance d'un trajet
    //@param : List<GeoPoint> points
    //@return double distance
    private double calculDistance(List<GeoPoint> points) {
        if (points.size() > 1) {
            double totalDistance = 0;
            for (int i = 0; i < points.size() - 1; i++) {
                GeoPoint startPoint = points.get(i);
                GeoPoint endPoint = points.get(i + 1);
                float[] results = new float[1];
                Location.distanceBetween(startPoint.getLatitudeE6() / 1E6, startPoint.getLongitudeE6() / 1E6,
                        endPoint.getLatitudeE6() / 1E6, endPoint.getLongitudeE6() / 1E6, results);

                totalDistance += results[0];
            }
            return totalDistance;
        }
        return 0;
    }

    private double calculCurrentSpeed(ArrayList<GeoPointHorodate> pointshorodates) {
        if (SystemClock.elapsedRealtime() - lastLocationChangedTime < 5000 && pointshorodates.size() >= 6) {
            int nbPoints = pointshorodates.size();
            GeoPointHorodate p1 = pointshorodates.get(nbPoints - 5);
            GeoPointHorodate p2 = pointshorodates.get(nbPoints - 1);
            double time = p2.getHeure() - p1.getHeure();
            ArrayList<GeoPoint> lastPoints = new ArrayList<GeoPoint>();
            for (int i = nbPoints - 5; i < nbPoints; i++) {
                lastPoints.add(points.get(i));
            }
            double distance = calculDistance(lastPoints);
            if (time != 0) {
                return 3600 * distance / time;
            }

        }
        return 0;
    }




    private void updateLine() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            points = (ArrayList<GeoPoint>) pointshorodates.stream().map(p -> (GeoPoint) p).collect(Collectors.toList());
            //Log.d(TAG, "conversion des points, nbpoints : " + points.size() + " " + pointshorodates.size());
        }
        line.setPoints(points);
    }

    private void updateDistance() {
        distanceTotale = calculDistance(points);
        distanceTextView.setText(String.valueOf(Math.round(distanceTotale)));
        //Log.d(TAG, "distance actuelle : " + Math.round(distanceTotale) + "    nb de points : " + points.size()+  "   vitesse moyenne : " + averageSpeed + "   vitesse courante : " + currentSpeed);
    }

    private void updateTrip() {
        if (started) {
            updateLine();
            updateDistance();
        }
    }

    private void manageNewLocation(Location location) {
        //updateAccuracyText(location);
        if (started) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //Log.d(TAG, "nouvelle coordonnée détectée  : " + latitude + "  " + longitude);
            GeoPoint newPoint = new GeoPoint(latitude, longitude);
            if (location.getAccuracy() <= 8) {
                lastLocationChangedTime = SystemClock.elapsedRealtime();
                points.add(newPoint);
                pointshorodates.add(new GeoPointHorodate(newPoint, SystemClock.elapsedRealtime(), 0));
                updateLine();
                updateDistance();
            }
        }

    }
}