package com.drop.drop_backend.service;


import com.drop.drop_backend.dto.RoutePoint;
import com.drop.drop_backend.dto.RouteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    private static final String ROUTING_URL =
            "https://api.geoapify.com/v1/routing";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public RouteService(
            ObjectMapper objectMapper,
            @Value("${geoapify.api-key}") String apiKey
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;

        this.restClient = RestClient.builder()
                .baseUrl(ROUTING_URL)
                .build();
    }

    public RouteResponse getRoute(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude,
            String mode
    ) {

        String waypoints =
                startLatitude + "," + startLongitude
                        + "|"
                        + endLatitude + "," + endLongitude;

        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("waypoints", waypoints)
                        .queryParam("mode", mode)
                        .queryParam("format", "geojson")
                        .queryParam("apiKey", apiKey)
                        .build()
                )
                .retrieve()
                .body(String.class);

        return parseRoute(response);
    }

    private RouteResponse parseRoute(String response) {

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode features =
                    root.get("features");

            if (features == null || !features.isArray()
                    || features.isEmpty()) {

                throw new RuntimeException(
                        "No route found"
                );
            }

            JsonNode feature =
                    features.get(0);

            JsonNode properties =
                    feature.get("properties");

            double distance =
                    properties
                            .get("distance")
                            .asDouble();

            double duration =
                    properties
                            .get("time")
                            .asDouble();

            JsonNode geometry =
                    feature.get("geometry");

            JsonNode coordinates =
                    geometry.get("coordinates");

            List<RoutePoint> routePoints =
                    new ArrayList<>();

            addCoordinates(
                    coordinates,
                    routePoints
            );

            return new RouteResponse(
                    distance,
                    duration,
                    routePoints
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Geoapify route response",
                    e
            );
        }
    }

    private void addCoordinates(
            JsonNode node,
            List<RoutePoint> routePoints
    ) {

        if (node == null || !node.isArray()) {
            return;
        }

        /*
         * GeoJSON coordinates are:
         *
         * [longitude, latitude]
         */

        if (node.size() >= 2
                && node.get(0).isNumber()
                && node.get(1).isNumber()) {

            double longitude =
                    node.get(0).asDouble();

            double latitude =
                    node.get(1).asDouble();

            routePoints.add(
                    new RoutePoint(
                            latitude,
                            longitude
                    )
            );

            return;
        }

        for (JsonNode child : node) {

            addCoordinates(
                    child,
                    routePoints
            );
        }
    }
}
