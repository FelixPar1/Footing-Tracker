package com.example.footingtrainmap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trip {
    private LocalDate date;
    private String nom;
    private Date heureDebut;
    private Date heureFin;
    private float duration;
    private double distance;
    private double vitesse;
    private ArrayList<GeoPointHorodate> points;

    public Trip( String nom, LocalDate date, Date heureDebut, Date heureFin, int duration, double distance, double vitesse, ArrayList<GeoPointHorodate> points) {
        this.nom = nom;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.duration = duration;
        this.distance = distance;
        this.vitesse = vitesse;
        this.points = points;
    }

    public Trip( LocalDate date, Date heureDebut) {
        this("", date, heureDebut, null, 0, 0, 0, new ArrayList<GeoPointHorodate>());
    }

    public String getNom() { return nom; }

    public void setNom(String nom) { this.nom = nom; }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Date getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(Date heureDebut) {
        this.heureDebut = heureDebut;
    }

    public Date getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(Date heureFin) {
        this.heureFin = heureFin;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getVitesse() { return vitesse; }

    public void setVitesse(double vitesse) { this.vitesse = vitesse; }

    public List<GeoPointHorodate> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<GeoPointHorodate> points) {
        this.points = points;
    }

}
