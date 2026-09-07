package com.drop.drop_backend.service;

import com.drop.drop_backend.Repository.PlaceRepository;
import com.drop.drop_backend.dto.PlaceResponse;
import com.drop.drop_backend.entity.Place;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PlaceService {

    private static final int MAX_RADIUS_METERS = 10_000;

    private static final int RANDOM_RESTAURANT_COUNT = 8;

    private final PlaceRepository placeRepository;

    private final GeoapifyService geoapifyService;

    private final RedisGeoService redisGeoService;

    public PlaceService(
            PlaceRepository placeRepository,
            GeoapifyService geoapifyService,
            RedisGeoService redisGeoService
    ) {
        this.placeRepository = placeRepository;
        this.geoapifyService = geoapifyService;
        this.redisGeoService = redisGeoService;
    }

    // =========================================================
    // NEARBY RESTAURANTS
    // =========================================================

    public List<PlaceResponse> getNearbyRestaurants(
            double latitude,
            double longitude,
            int radius
    ) {

        if (!isValidCoordinates(
                latitude,
                longitude
        )) {
            return List.of();
        }

        int safeRadius =
                Math.min(
                        Math.max(
                                radius,
                                1
                        ),
                        MAX_RADIUS_METERS
                );

        double radiusKm =
                safeRadius / 1000.0;

        // =====================================================
        // 1. REDIS GEO
        // =====================================================

        List<PlaceResponse> restaurants =
                redisGeoService.findNearbyRestaurants(
                        latitude,
                        longitude,
                        radiusKm
                );

        if (!restaurants.isEmpty()) {
            return restaurants;
        }

        // =====================================================
        // 2. REDIS MISS → GEOAPIFY
        // =====================================================

        List<PlaceResponse> freshRestaurants =
                geoapifyService.getNearbyRestaurants(
                        latitude,
                        longitude
                );

        if (
                freshRestaurants == null
                        || freshRestaurants.isEmpty()
        ) {
            return List.of();
        }

        // =====================================================
        // 3. BATCH SAVE TO MYSQL
        // =====================================================

        try {

            saveRestaurantsToDatabase(
                    freshRestaurants
            );

        } catch (Exception ignored) {
            // Redis can still serve the restaurants.
        }

        // =====================================================
        // 4. SAVE TO REDIS
        // =====================================================

        for (
                PlaceResponse restaurant
                : freshRestaurants
        ) {

            if (restaurant == null) {
                continue;
            }

            try {

                redisGeoService.saveRestaurant(
                        restaurant
                );

            } catch (Exception ignored) {
                // Continue with remaining restaurants.
            }
        }

        // =====================================================
        // 5. READ FROM REDIS AGAIN
        // =====================================================

        restaurants =
                redisGeoService.findNearbyRestaurants(
                        latitude,
                        longitude,
                        radiusKm
                );

        // =====================================================
        // 6. FALLBACK
        // =====================================================

        if (restaurants.isEmpty()) {
            return freshRestaurants;
        }

        return restaurants;
    }

    // =========================================================
    // RESTAURANT DETAIL
    // =========================================================

    @Transactional(readOnly = true)
    public PlaceResponse getRestaurantById(
            Long osmId
    ) {

        if (
                osmId == null
                        || osmId <= 0
        ) {
            return null;
        }

        // =====================================================
        // 1. REDIS CACHE
        // =====================================================

        PlaceResponse cachedRestaurant =
                redisGeoService.getRestaurantById(
                        osmId
                );

        if (cachedRestaurant != null) {
            return cachedRestaurant;
        }

        // =====================================================
        // 2. MYSQL FALLBACK
        // =====================================================

        Place place =
                placeRepository
                        .findByOsmId(osmId)
                        .orElse(null);

        if (place == null) {
            return null;
        }

        // =====================================================
        // 3. CONVERT
        // =====================================================

        PlaceResponse restaurant =
                convertToResponse(
                        place
                );

        // =====================================================
        // 4. CACHE IN REDIS
        // =====================================================

        redisGeoService.cacheRestaurant(
                restaurant
        );

        return restaurant;
    }

    // =========================================================
    // RANDOM RESTAURANTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PlaceResponse> getRandomRestaurants() {

        // =====================================================
        // 1. REDIS CACHE
        // =====================================================

        List<PlaceResponse> cachedRestaurants =
                redisGeoService.getRandomRestaurants();

        if (
                cachedRestaurants != null
                        && !cachedRestaurants.isEmpty()
        ) {
            return cachedRestaurants;
        }

        // =====================================================
        // 2. GET MAX ID
        // =====================================================

        long maxId =
                placeRepository
                        .findTopByOrderByIdDesc()
                        .map(Place::getId)
                        .orElse(0L);

        if (maxId <= 0) {
            return List.of();
        }

        // =====================================================
        // 3. RANDOM STARTING ID
        // =====================================================

        long randomId =
                ThreadLocalRandom.current()
                        .nextLong(
                                1,
                                maxId + 1
                        );

        // =====================================================
        // 4. SEARCH FORWARD
        // =====================================================

        List<Place> places =
                placeRepository
                        .findTop8ByTypeAndIdGreaterThan(
                                "restaurant",
                                randomId
                        );

        // =====================================================
        // 5. WRAP AROUND
        // =====================================================

        if (
                places.size()
                        < RANDOM_RESTAURANT_COUNT
        ) {

            List<Place> fallback =
                    placeRepository
                            .findTop8ByTypeAndIdLessThan(
                                    "restaurant",
                                    randomId
                            );

            for (
                    Place place
                    : fallback
            ) {

                if (
                        places.size()
                                >= RANDOM_RESTAURANT_COUNT
                ) {
                    break;
                }

                places.add(place);
            }
        }

        if (places.isEmpty()) {
            return List.of();
        }

        // =====================================================
        // 6. CONVERT
        // =====================================================

        List<PlaceResponse> restaurants =
                new ArrayList<>(
                        places.size()
                );

        for (
                Place place
                : places
        ) {

            if (place == null) {
                continue;
            }

            restaurants.add(
                    convertToResponse(
                            place
                    )
            );
        }

        // =====================================================
        // 7. CACHE
        // =====================================================

        if (!restaurants.isEmpty()) {

            redisGeoService.cacheRandomRestaurants(
                    restaurants
            );
        }

        return restaurants;
    }

    // =========================================================
    // BATCH SAVE RESTAURANTS
    // =========================================================

    @Transactional
    protected void saveRestaurantsToDatabase(
            List<PlaceResponse> restaurants
    ) {

        if (
                restaurants == null
                        || restaurants.isEmpty()
        ) {
            return;
        }

        // =====================================================
        // COLLECT OSM IDS
        // =====================================================

        List<Long> osmIds =
                new ArrayList<>();

        for (
                PlaceResponse restaurant
                : restaurants
        ) {

            if (
                    restaurant != null
                            && restaurant.getOsmId() != null
            ) {

                osmIds.add(
                        restaurant.getOsmId()
                );
            }
        }

        // =====================================================
        // FIND EXISTING RESTAURANTS
        // =====================================================

        Map<Long, Place> existingPlaces =
                new HashMap<>();

        if (!osmIds.isEmpty()) {

            List<Place> existing =
                    placeRepository.findByOsmIdIn(
                            osmIds
                    );

            for (
                    Place place
                    : existing
            ) {

                if (
                        place != null
                                && place.getOsmId() != null
                ) {

                    existingPlaces.put(
                            place.getOsmId(),
                            place
                    );
                }
            }
        }

        // =====================================================
        // BUILD BATCH
        // =====================================================

        List<Place> placesToSave =
                new ArrayList<>(
                        restaurants.size()
                );

        for (
                PlaceResponse restaurant
                : restaurants
        ) {

            if (restaurant == null) {
                continue;
            }

            Place place = null;

            if (restaurant.getOsmId() != null) {

                place =
                        existingPlaces.get(
                                restaurant.getOsmId()
                        );
            }

            if (place == null) {
                place = new Place();
            }

            updatePlace(
                    place,
                    restaurant
            );

            placesToSave.add(
                    place
            );
        }

        // =====================================================
        // ONE BATCH SAVE
        // =====================================================

        if (!placesToSave.isEmpty()) {

            placeRepository.saveAll(
                    placesToSave
            );

            placeRepository.flush();
        }
    }

    // =========================================================
    // UPDATE PLACE
    // =========================================================

    private void updatePlace(
            Place place,
            PlaceResponse restaurant
    ) {

        place.setOsmId(
                restaurant.getOsmId()
        );

        place.setName(
                safe(
                        restaurant.getName()
                )
        );

        place.setType(
                safe(
                        restaurant.getType()
                )
        );

        place.setCategory(
                safe(
                        restaurant.getCategory()
                )
        );

        place.setCuisine(
                safe(
                        restaurant.getCuisine()
                )
        );

        place.setAddress(
                safe(
                        restaurant.getAddress()
                )
        );

        place.setPhone(
                safe(
                        restaurant.getPhone()
                )
        );

        place.setWebsite(
                safe(
                        restaurant.getWebsite()
                )
        );

        place.setLatitude(
                restaurant.getLatitude()
        );

        place.setLongitude(
                restaurant.getLongitude()
        );
    }

    // =========================================================
    // PLACE → RESPONSE
    // =========================================================

    private PlaceResponse convertToResponse(
            Place place
    ) {

        PlaceResponse response =
                new PlaceResponse();

        response.setOsmId(
                place.getOsmId()
        );

        response.setName(
                safe(
                        place.getName()
                )
        );

        response.setType(
                safe(
                        place.getType()
                )
        );

        response.setCategory(
                safe(
                        place.getCategory()
                )
        );

        response.setCuisine(
                safe(
                        place.getCuisine()
                )
        );

        response.setAddress(
                safe(
                        place.getAddress()
                )
        );

        response.setPhone(
                safe(
                        place.getPhone()
                )
        );

        response.setWebsite(
                safe(
                        place.getWebsite()
                )
        );

        response.setLatitude(
                place.getLatitude()
        );

        response.setLongitude(
                place.getLongitude()
        );

        response.setDistance(
                null
        );

        return response;
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

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}