package com.drop.drop_backend.controller;

import com.drop.drop_backend.dto.PlaceResponse;
import com.drop.drop_backend.service.PlaceService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(
            PlaceService placeService
    ) {
        this.placeService =
                placeService;
    }

    // =========================================================
    // NEARBY RESTAURANTS
    // =========================================================

    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceResponse>> getNearbyRestaurants(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "3000") int radius
    ) {

        // =====================================================
        // VALIDATE COORDINATES
        // =====================================================

        if (
                latitude < -90
                        || latitude > 90
                        || longitude < -180
                        || longitude > 180
        ) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // =====================================================
        // VALIDATE RADIUS
        // =====================================================

        if (radius <= 0) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // =====================================================
        // SAFE RADIUS
        // =====================================================

        int safeRadius =
                Math.min(
                        radius,
                        10000
                );

        // =====================================================
        // GET RESTAURANTS
        // =====================================================

        List<PlaceResponse> restaurants =
                placeService.getNearbyRestaurants(
                        latitude,
                        longitude,
                        safeRadius
                );

        // =====================================================
        // RESPONSE
        // =====================================================

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.maxAge(
                                30,
                                TimeUnit.SECONDS
                        ).cachePublic()
                )
                .body(
                        restaurants
                );
    }

    // =========================================================
    // RANDOM RESTAURANTS
    // =========================================================

    @GetMapping("/random")
    public ResponseEntity<List<PlaceResponse>> getRandomRestaurants() {

        List<PlaceResponse> restaurants =
                placeService.getRandomRestaurants();

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.maxAge(
                                60,
                                TimeUnit.SECONDS
                        ).cachePublic()
                )
                .body(
                        restaurants
                );
    }

    // =========================================================
    // RESTAURANT DETAIL
    // =========================================================

    @GetMapping("/{osmId}")
    public ResponseEntity<PlaceResponse> getRestaurantById(
            @PathVariable Long osmId
    ) {

        // =====================================================
        // VALIDATE ID
        // =====================================================

        if (
                osmId == null
                        || osmId <= 0
        ) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        // =====================================================
        // GET RESTAURANT
        // =====================================================

        PlaceResponse restaurant =
                placeService.getRestaurantById(
                        osmId
                );

        // =====================================================
        // NOT FOUND
        // =====================================================

        if (restaurant == null) {

            return ResponseEntity
                    .status(
                            HttpStatus.NOT_FOUND
                    )
                    .build();
        }

        // =====================================================
        // CACHE RESPONSE
        // =====================================================

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.maxAge(
                                60,
                                TimeUnit.SECONDS
                        ).cachePublic()
                )
                .body(
                        restaurant
                );
    }
}