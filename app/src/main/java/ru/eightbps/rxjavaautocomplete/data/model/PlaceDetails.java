package ru.eightbps.rxjavaautocomplete.data.model;

public class PlaceDetails {

    public Geometry geometry;
    public String name;

    @Override
    public String toString() {
        return "PlaceDetails{" +
                "geometry=" + geometry +
                ", name='" + name + '\'' +
                '}';
    }
}
