package com.example.footingtrainmap;

import android.os.Parcel;
import android.os.Parcelable;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

public class GeoPointHorodate extends GeoPoint implements Parcelable {
    private long heure;
    private long distance;

    public GeoPointHorodate(double latitude, double longitude, long heure, long distance) {
        super(latitude, longitude);
        this.heure = heure;
        this.distance = distance;
    }

    public GeoPointHorodate(GeoPoint geoPoint, long heure, long distance) {
        this(geoPoint.getLatitude(), geoPoint.getLongitude(), heure, distance);
    }

    protected GeoPointHorodate(Parcel in) {
        super(in.readDouble(), in.readDouble());
        heure = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
        dest.writeLong(heure);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GeoPointHorodate> CREATOR = new Creator<GeoPointHorodate>() {
        @Override
        public GeoPointHorodate createFromParcel(Parcel in) {
            return new GeoPointHorodate(in);
        }

        @Override
        public GeoPointHorodate[] newArray(int size) {
            return new GeoPointHorodate[size];
        }
    };

    public long getHeure() {
        return heure;
    }

    public void setHeure(long heure) {
        this.heure = heure;
    }

    public GeoPoint getPoint() {
        return new GeoPoint(getLatitude(), getLongitude());
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
