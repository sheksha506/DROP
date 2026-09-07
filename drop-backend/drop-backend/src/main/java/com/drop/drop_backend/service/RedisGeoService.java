package com.drop.drop_backend.service;

import com.drop.drop_backend.dto.PlaceResponse;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisGeoService {

    // =========================================================
    // REDIS KEYS
    // =========================================================

    private static final String RESTAURANT_GEO_KEY =
            "drop:restaurants:geo";

    private static final String RESTAURANT_KEY_PREFIX =
            "drop:restaurant:";

    private static final String RANDOM_RESTAURANTS_KEY =
            "drop:restaurants:random";

    // =========================================================
    // CACHE SETTINGS
    // =========================================================

    private static final int MAX_RESULTS = 50;

    private static final int RANDOM_RESULTS = 8;

    private static final long RANDOM_CACHE_SECONDS = 60;

    private static final long DETAIL_CACHE_SECONDS = 300;

    private static final double MAX_RADIUS_KM = 10.0;

    // =========================================================
    // DEPENDENCY
    // =========================================================

    private final StringRedisTemplate redisTemplate;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RedisGeoService(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate =
                redisTemplate;
    }

    // =========================================================
    // SAVE RESTAURANT
    // =========================================================

    public void saveRestaurant(
            PlaceResponse restaurant
    ) {

        if (
                restaurant == null
                        || restaurant.getOsmId() == null
                        || !isValidCoordinates(
                        restaurant.getLatitude(),
                        restaurant.getLongitude()
                )
        ) {
            return;
        }

        String restaurantId =
                getRestaurantId(
                        restaurant
                );

        String restaurantKey =
                RESTAURANT_KEY_PREFIX
                        + restaurantId;

        try {

            // =================================================
            // GEO LOCATION
            // =================================================

            redisTemplate.opsForGeo()
                    .add(
                            RESTAURANT_GEO_KEY,
                            new Point(
                                    restaurant.getLongitude(),
                                    restaurant.getLatitude()
                            ),
                            restaurantId
                    );

            // =================================================
            // RESTAURANT DETAIL
            // =================================================

            Map<String, String> data =
                    new HashMap<>();

            data.put(
                    "osmId",
                    safe(
                            restaurant.getOsmId()
                                    .toString()
                    )
            );

            data.put(
                    "name",
                    safe(
                            restaurant.getName()
                    )
            );

            data.put(
                    "type",
                    safe(
                            restaurant.getType()
                    )
            );

            data.put(
                    "category",
                    safe(
                            restaurant.getCategory()
                    )
            );

            data.put(
                    "cuisine",
                    safe(
                            restaurant.getCuisine()
                    )
            );

            data.put(
                    "address",
                    safe(
                            restaurant.getAddress()
                    )
            );

            data.put(
                    "phone",
                    safe(
                            restaurant.getPhone()
                    )
            );

            data.put(
                    "website",
                    safe(
                            restaurant.getWebsite()
                    )
            );

            data.put(
                    "latitude",
                    safe(
                            restaurant.getLatitude()
                                    .toString()
                    )
            );

            data.put(
                    "longitude",
                    safe(
                            restaurant.getLongitude()
                                    .toString()
                    )
            );

            redisTemplate.opsForHash()
                    .putAll(
                            restaurantKey,
                            data
                    );

            // =================================================
            // INVALIDATE RANDOM CACHE
            // =================================================

            redisTemplate.delete(
                    RANDOM_RESTAURANTS_KEY
            );

        } catch (Exception ignored) {
            // Redis failure must not break the API.
        }
    }

    // =========================================================
    // GET RESTAURANT DETAIL
    // =========================================================

    public PlaceResponse getRestaurantById(
            Long osmId
    ) {

        if (osmId == null) {
            return null;
        }

        String key =
                RESTAURANT_KEY_PREFIX
                        + osmId;

        try {

            Map<Object, Object> data =
                    redisTemplate.opsForHash()
                            .entries(key);

            if (
                    data == null
                            || data.isEmpty()
            ) {
                return null;
            }

            return convertRedisData(
                    osmId.toString(),
                    data
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // CACHE RESTAURANT DETAIL
    // =========================================================
    //
    // Restaurant data is already stored as a Redis hash.
    // The hash itself is persistent.
    //
    // We don't set a TTL here because this data is also used
    // by the GEO nearby-search functionality.
    //
    // The database remains the source of truth.
    // =========================================================

    public void cacheRestaurant(
            PlaceResponse restaurant
    ) {

        if (restaurant == null) {
            return;
        }

        saveRestaurant(
                restaurant
        );
    }

    // =========================================================
    // INVALIDATE RESTAURANT DETAIL
    // =========================================================

    public void invalidateRestaurant(
            Long osmId
    ) {

        if (osmId == null) {
            return;
        }

        String restaurantId =
                osmId.toString();

        try {

            redisTemplate.delete(
                    RESTAURANT_KEY_PREFIX
                            + restaurantId
            );

            redisTemplate.opsForGeo()
                    .remove(
                            RESTAURANT_GEO_KEY,
                            restaurantId
                    );

            redisTemplate.delete(
                    RANDOM_RESTAURANTS_KEY
            );

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // GET RANDOM RESTAURANTS
    // =========================================================

    public List<PlaceResponse> getRandomRestaurants() {

        try {

            List<String> ids =
                    redisTemplate.opsForList()
                            .range(
                                    RANDOM_RESTAURANTS_KEY,
                                    0,
                                    RANDOM_RESULTS - 1
                            );

            if (
                    ids == null
                            || ids.isEmpty()
            ) {
                return List.of();
            }

            List<PlaceResponse> restaurants =
                    getRestaurantsByIds(
                            ids
                    );

            return restaurants;

        } catch (Exception ignored) {

            return List.of();
        }
    }

    // =========================================================
    // CACHE RANDOM RESTAURANTS
    // =========================================================

    public void cacheRandomRestaurants(
            List<PlaceResponse> restaurants
    ) {

        if (
                restaurants == null
                        || restaurants.isEmpty()
        ) {
            return;
        }

        try {

            redisTemplate.delete(
                    RANDOM_RESTAURANTS_KEY
            );

            int count = 0;

            for (
                    PlaceResponse restaurant
                    : restaurants
            ) {

                if (
                        restaurant == null
                                || restaurant.getOsmId() == null
                ) {
                    continue;
                }

                redisTemplate.opsForList()
                        .rightPush(
                                RANDOM_RESTAURANTS_KEY,
                                getRestaurantId(
                                        restaurant
                                )
                        );

                count++;

                if (
                        count >= RANDOM_RESULTS
                ) {
                    break;
                }
            }

            if (count > 0) {

                redisTemplate.expire(
                        RANDOM_RESTAURANTS_KEY,
                        RANDOM_CACHE_SECONDS,
                        TimeUnit.SECONDS
                );
            }

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // FIND NEARBY RESTAURANTS
    // =========================================================

    public List<PlaceResponse> findNearbyRestaurants(
            double latitude,
            double longitude,
            double radiusKm
    ) {

        if (
                !isValidCoordinates(
                        latitude,
                        longitude
                )
        ) {
            return List.of();
        }

        double safeRadius =
                Math.min(
                        Math.max(
                                radiusKm,
                                0.1
                        ),
                        MAX_RADIUS_KM
                );

        try {

            GeoResults<RedisGeoCommands.GeoLocation<String>>
                    geoResults =
                    redisTemplate.opsForGeo()
                            .radius(
                                    RESTAURANT_GEO_KEY,
                                    new Circle(
                                            new Point(
                                                    longitude,
                                                    latitude
                                            ),
                                            new Distance(
                                                    safeRadius,
                                                    Metrics.KILOMETERS
                                            )
                                    ),
                                    RedisGeoCommands.GeoRadiusCommandArgs
                                            .newGeoRadiusArgs()
                                            .includeDistance()
                                            .sortAscending()
                                            .limit(
                                                    MAX_RESULTS
                                            )
                            );

            if (
                    geoResults == null
                            || geoResults.getContent()
                            .isEmpty()
            ) {
                return List.of();
            }

            List<String> ids =
                    new ArrayList<>();

            Map<String, Double> distances =
                    new HashMap<>();

            for (
                    GeoResult<
                            RedisGeoCommands.GeoLocation<String>
                            > result
                    : geoResults
                    .getContent()
            ) {

                String id =
                        result.getContent()
                                .getName();

                if (
                        id == null
                                || id.isBlank()
                ) {
                    continue;
                }

                ids.add(id);

                if (
                        result.getDistance() != null
                ) {

                    distances.put(
                            id,
                            result.getDistance()
                                    .getValue()
                    );
                }
            }

            List<PlaceResponse> restaurants =
                    getRestaurantsByIds(
                            ids
                    );

            for (
                    PlaceResponse restaurant
                    : restaurants
            ) {

                if (
                        restaurant == null
                                || restaurant.getOsmId() == null
                ) {
                    continue;
                }

                Double distance =
                        distances.get(
                                restaurant
                                        .getOsmId()
                                        .toString()
                        );

                restaurant.setDistance(
                        distance
                );
            }

            return restaurants;

        } catch (Exception ignored) {

            return List.of();
        }
    }

    // =========================================================
    // PIPELINE RESTAURANT READS
    // =========================================================

    private List<PlaceResponse> getRestaurantsByIds(
            List<String> ids
    ) {

        if (
                ids == null
                        || ids.isEmpty()
        ) {
            return List.of();
        }

        try {

            List<Object> results =
                    redisTemplate.executePipelined(
                            (RedisCallback<Object>) connection -> {

                                for (
                                        String id
                                        : ids
                                ) {

                                    connection.hashCommands()
                                            .hGetAll(
                                                    (
                                                            RESTAURANT_KEY_PREFIX
                                                                    + id
                                                    )
                                                            .getBytes(
                                                                    StandardCharsets.UTF_8
                                                            )
                                            );
                                }

                                return null;
                            }
                    );

            List<PlaceResponse> restaurants =
                    new ArrayList<>();

            for (
                    int i = 0;
                    i < ids.size();
                    i++
            ) {

                if (
                        i >= results.size()
                ) {
                    break;
                }

                Object result =
                        results.get(i);

                if (
                        !(result instanceof Map)
                                || ((Map<?, ?>) result)
                                .isEmpty()
                ) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<Object, Object> data =
                        (Map<Object, Object>) result;

                PlaceResponse restaurant =
                        convertRedisData(
                                ids.get(i),
                                data
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
    // CONVERT REDIS DATA
    // =========================================================

    private PlaceResponse convertRedisData(
            String restaurantId,
            Map<Object, Object> data
    ) {

        if (
                data == null
                        || data.isEmpty()
        ) {
            return null;
        }

        PlaceResponse response =
                new PlaceResponse();

        String osmId =
                getValue(
                        data,
                        "osmId"
                );

        if (
                osmId == null
                        || osmId.isBlank()
        ) {
            osmId = restaurantId;
        }

        try {

            response.setOsmId(
                    Long.parseLong(
                            osmId
                    )
            );

        } catch (NumberFormatException ignored) {

            return null;
        }

        response.setName(
                safe(
                        getValue(
                                data,
                                "name"
                        )
                )
        );

        response.setType(
                safe(
                        getValue(
                                data,
                                "type"
                        )
                )
        );

        response.setCategory(
                safe(
                        getValue(
                                data,
                                "category"
                        )
                )
        );

        response.setCuisine(
                safe(
                        getValue(
                                data,
                                "cuisine"
                        )
                )
        );

        response.setAddress(
                safe(
                        getValue(
                                data,
                                "address"
                        )
                )
        );

        response.setPhone(
                safe(
                        getValue(
                                data,
                                "phone"
                        )
                )
        );

        response.setWebsite(
                safe(
                        getValue(
                                data,
                                "website"
                        )
                )
        );

        String latitude =
                getValue(
                        data,
                        "latitude"
                );

        String longitude =
                getValue(
                        data,
                        "longitude"
                );

        try {

            response.setLatitude(
                    latitude == null
                            ? null
                            : Double.parseDouble(
                            latitude
                    )
            );

            response.setLongitude(
                    longitude == null
                            ? null
                            : Double.parseDouble(
                            longitude
                    )
            );

        } catch (NumberFormatException ignored) {

            response.setLatitude(null);
            response.setLongitude(null);
        }

        response.setDistance(
                null
        );

        return response;
    }

    // =========================================================
    // GET HASH VALUE
    // =========================================================

    private String getValue(
            Map<Object, Object> data,
            String key
    ) {

        Object value =
                data.get(key);

        if (value == null) {
            value =
                    data.get(
                            key.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );
        }

        return value == null
                ? null
                : value.toString();
    }

    // =========================================================
    // RESTAURANT ID
    // =========================================================

    private String getRestaurantId(
            PlaceResponse restaurant
    ) {

        return restaurant
                .getOsmId()
                .toString();
    }

    // =========================================================
    // VALIDATE COORDINATES
    // =========================================================

    private boolean isValidCoordinates(
            Double latitude,
            Double longitude
    ) {

        if (
                latitude == null
                        || longitude == null
        ) {
            return false;
        }

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