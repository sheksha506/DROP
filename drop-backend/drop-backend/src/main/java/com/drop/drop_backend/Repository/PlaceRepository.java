package com.drop.drop_backend.Repository;

import com.drop.drop_backend.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository
        extends JpaRepository<Place, Long> {

    // =========================================================
    // FIND BY OSM ID
    // =========================================================

    Optional<Place> findByOsmId(Long osmId);

    // =========================================================
    // FIND MULTIPLE RESTAURANTS BY OSM IDS
    // =========================================================

    List<Place> findByOsmIdIn(
            List<Long> osmIds
    );

    // =========================================================
    // HIGHEST PLACE ID
    // =========================================================

    Optional<Place> findTopByOrderByIdDesc();

    // =========================================================
    // RANDOM RESTAURANTS
    // =========================================================

    List<Place> findTop8ByTypeAndIdGreaterThan(
            String type,
            Long id
    );

    // =========================================================
    // RANDOM FALLBACK
    // =========================================================

    List<Place> findTop8ByTypeAndIdLessThan(
            String type,
            Long id
    );
}