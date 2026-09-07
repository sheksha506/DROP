package com.drop.drop_backend.service;

import com.drop.drop_backend.dto.PlaceResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoapifyService {

    private static final String GEOAPIFY_URL =
            "https://api.geoapify.com/v2/places";

    private static final int SEARCH_RADIUS =
            1000;

    private static final int RESULT_LIMIT =
            20;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    private final String apiKey;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GeoapifyService(
            ObjectMapper objectMapper,
            @Value("${geoapify.api-key}") String apiKey,
            ClientHttpRequestFactory geoapifyRequestFactory
    ) {

        this.objectMapper =
                objectMapper;

        this.apiKey =
                apiKey;

        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                GEOAPIFY_URL
                        )
                        .requestFactory(
                                geoapifyRequestFactory
                        )
                        .build();
    }

    // =========================================================
    // GET NEARBY RESTAURANTS
    // =========================================================

    public List<PlaceResponse> getNearbyRestaurants(
            double latitude,
            double longitude
    ) {

        if (!isValidCoordinates(
                latitude,
                longitude
        )) {
            return List.of();
        }

        try {

            String response =
                    restClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .queryParam(
                                                            "categories",
                                                            "catering.restaurant"
                                                    )
                                                    .queryParam(
                                                            "filter",
                                                            String.format(
                                                                    "circle:%f,%f,%d",
                                                                    longitude,
                                                                    latitude,
                                                                    SEARCH_RADIUS
                                                            )
                                                    )
                                                    .queryParam(
                                                            "limit",
                                                            RESULT_LIMIT
                                                    )
                                                    .queryParam(
                                                            "apiKey",
                                                            apiKey
                                                    )
                                                    .build()
                            )
                            .retrieve()
                            .body(
                                    String.class
                            );

            if (
                    response == null
                            || response.isBlank()
            ) {
                return List.of();
            }

            return parseResponse(
                    response,
                    latitude,
                    longitude
            );

        } catch (Exception ignored) {

            return List.of();
        }
    }

    // =========================================================
    // PARSE RESPONSE
    // =========================================================

    private List<PlaceResponse> parseResponse(
            String response,
            double userLatitude,
            double userLongitude
    ) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            response
                    );

            JsonNode features =
                    root.get(
                            "features"
                    );

            if (
                    features == null
                            || !features.isArray()
            ) {
                return List.of();
            }

            List<PlaceResponse> restaurants =
                    new ArrayList<>();

            for (
                    JsonNode feature
                    : features
            ) {

                PlaceResponse restaurant =
                        parsePlace(
                                feature,
                                userLatitude,
                                userLongitude
                        );

                if (restaurant != null) {

                    restaurants.add(
                            restaurant
                    );
                }
            }

            return restaurants;

        } catch (Exception ignored) {

            return List.of();
        }
    }

    // =========================================================
    // PARSE PLACE
    // =========================================================

    private PlaceResponse parsePlace(
            JsonNode feature,
            double userLatitude,
            double userLongitude
    ) {

        try {

            JsonNode properties =
                    feature.get(
                            "properties"
                    );

            if (
                    properties == null
                            || properties.isMissingNode()
            ) {
                return null;
            }

            // =================================================
            // OSM ID
            // =================================================

            Long osmId =
                    getLong(
                            properties,
                            "osm_id"
                    );

            if (osmId == null) {
                return null;
            }

            // =================================================
            // COORDINATES
            // =================================================

            Double longitude =
                    getDouble(
                            properties,
                            "lon"
                    );

            Double latitude =
                    getDouble(
                            properties,
                            "lat"
                    );

            if (
                    latitude == null
                            || longitude == null
            ) {

                JsonNode geometry =
                        feature.get(
                                "geometry"
                        );

                if (
                        geometry != null
                                && geometry.has(
                                "coordinates"
                        )
                ) {

                    JsonNode coordinates =
                            geometry.get(
                                    "coordinates"
                            );

                    if (
                            coordinates != null
                                    && coordinates.isArray()
                                    && coordinates.size() >= 2
                    ) {

                        longitude =
                                coordinates
                                        .get(0)
                                        .asDouble();

                        latitude =
                                coordinates
                                        .get(1)
                                        .asDouble();
                    }
                }
            }

            if (
                    latitude == null
                            || longitude == null
            ) {
                return null;
            }

            // =================================================
            // RESPONSE
            // =================================================

            PlaceResponse restaurant =
                    new PlaceResponse();

            restaurant.setOsmId(
                    osmId
            );

            restaurant.setName(
                    getText(
                            properties,
                            "name"
                    )
            );

            restaurant.setType(
                    "restaurant"
            );

            restaurant.setCategory(
                    getText(
                            properties,
                            "categories"
                    )
            );

            restaurant.setCuisine(
                    getCuisine(
                            properties
                    )
            );

            restaurant.setAddress(
                    getAddress(
                            properties
                    )
            );

            restaurant.setPhone(
                    getNestedText(
                            properties,
                            "contact",
                            "phone"
                    )
            );

            restaurant.setWebsite(
                    getNestedText(
                            properties,
                            "contact",
                            "website"
                    )
            );

            restaurant.setLatitude(
                    latitude
            );

            restaurant.setLongitude(
                    longitude
            );

            restaurant.setDistance(
                    calculateDistance(
                            userLatitude,
                            userLongitude,
                            latitude,
                            longitude
                    )
            );

            return restaurant;

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // TEXT
    // =========================================================

    private String getText(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(
                        field
                );

        if (
                value == null
                        || value.isNull()
        ) {
            return "";
        }

        return value.asText(
                ""
        );
    }

    // =========================================================
    // NESTED TEXT
    // =========================================================

    private String getNestedText(
            JsonNode node,
            String parent,
            String child
    ) {

        JsonNode parentNode =
                node.get(
                        parent
                );

        if (
                parentNode == null
                        || parentNode.isNull()
        ) {
            return "";
        }

        JsonNode childNode =
                parentNode.get(
                        child
                );

        if (
                childNode == null
                        || childNode.isNull()
        ) {
            return "";
        }

        return childNode.asText(
                ""
        );
    }

    // =========================================================
    // CUISINE
    // =========================================================

    private String getCuisine(
            JsonNode properties
    ) {

        String cuisine =
                getText(
                        properties,
                        "catering.cuisine"
                );

        if (!cuisine.isBlank()) {
            return cuisine;
        }

        JsonNode catering =
                properties.get(
                        "catering"
                );

        if (
                catering != null
                        && catering.isObject()
        ) {

            JsonNode cuisineNode =
                    catering.get(
                            "cuisine"
                    );

            if (
                    cuisineNode != null
                            && !cuisineNode.isNull()
            ) {

                return cuisineNode.asText(
                        ""
                );
            }
        }

        return "";
    }

    // =========================================================
    // ADDRESS
    // =========================================================

    private String getAddress(
            JsonNode properties
    ) {

        String formatted =
                getText(
                        properties,
                        "formatted"
                );

        if (!formatted.isBlank()) {
            return formatted;
        }

        String addressLine1 =
                getText(
                        properties,
                        "address_line1"
                );

        String addressLine2 =
                getText(
                        properties,
                        "address_line2"
                );

        if (
                !addressLine1.isBlank()
                        && !addressLine2.isBlank()
        ) {

            return addressLine1
                    + ", "
                    + addressLine2;
        }

        if (!addressLine1.isBlank()) {
            return addressLine1;
        }

        return addressLine2;
    }

    // =========================================================
    // LONG
    // =========================================================

    private Long getLong(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(
                        field
                );

        if (
                value == null
                        || value.isNull()
        ) {
            return null;
        }

        if (value.isNumber()) {
            return value.longValue();
        }

        try {

            return Long.parseLong(
                    value.asText()
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // DOUBLE
    // =========================================================

    private Double getDouble(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(
                        field
                );

        if (
                value == null
                        || value.isNull()
        ) {
            return null;
        }

        if (value.isNumber()) {
            return value.doubleValue();
        }

        try {

            return Double.parseDouble(
                    value.asText()
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // DISTANCE
    // =========================================================

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        final double earthRadiusKm =
                6371.0;

        double latDistance =
                Math.toRadians(
                        lat2 - lat1
                );

        double lonDistance =
                Math.toRadians(
                        lon2 - lon1
                );

        double a =
                Math.sin(
                        latDistance / 2
                )
                        * Math.sin(
                        latDistance / 2
                )
                        + Math.cos(
                        Math.toRadians(
                                lat1
                        )
                )
                        * Math.cos(
                        Math.toRadians(
                                lat2
                        )
                )
                        * Math.sin(
                        lonDistance / 2
                )
                        * Math.sin(
                        lonDistance / 2
                );

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return earthRadiusKm * c;
    }

    // =========================================================
    // VALIDATE COORDINATES
    // =========================================================

    private boolean isValidCoordinates(
            double latitude,
            double longitude
    ) {

        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180;
    }
}