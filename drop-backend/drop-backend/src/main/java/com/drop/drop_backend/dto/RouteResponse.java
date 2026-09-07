package com.drop.drop_backend.dto;



import java.util.List;

public class RouteResponse {

    private double distance;
    private double duration;
    private List<RoutePoint> route;

    public RouteResponse(
            double distance,
            double duration,
            List<RoutePoint> route
    ) {
        this.distance = distance;
        this.duration = duration;
        this.route = route;
    }

    public double getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public List<RoutePoint> getRoute() {
        return route;
    }
}
