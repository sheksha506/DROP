package com.drop.drop_backend.dto;

public class RoutePoint {

    private double latitude;
    private double longitude;

    public RoutePoint(
            double latitude,
            double longitude
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
