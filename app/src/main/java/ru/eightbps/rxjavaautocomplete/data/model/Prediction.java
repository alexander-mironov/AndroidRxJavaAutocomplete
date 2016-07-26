package ru.eightbps.rxjavaautocomplete.data.model;

import com.squareup.moshi.Json;

public class Prediction {

    public String description;

    public String id;

    @Json(name = "place_id")
    public String placeId;

    @Override
    public String toString() {
        return "Prediction{" +
                "description='" + description + '\'' +
                ", id='" + id + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
