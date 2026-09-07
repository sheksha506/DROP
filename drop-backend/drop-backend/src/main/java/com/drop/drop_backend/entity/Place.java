package com.drop.drop_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "places",
        indexes = {

                // Fast restaurant filtering
                @Index(
                        name = "idx_place_type",
                        columnList = "type"
                ),

                // Fast OSM lookup
                @Index(
                        name = "idx_place_osm_id",
                        columnList = "osm_id"
                ),

                // Fast category filtering
                @Index(
                        name = "idx_place_category",
                        columnList = "category"
                ),

                // Fast cuisine filtering
                @Index(
                        name = "idx_place_cuisine",
                        columnList = "cuisine"
                ),

                // Helps queries that filter restaurants
                // and then work with location data.
                @Index(
                        name = "idx_place_type_lat_lon",
                        columnList = "type, latitude, longitude"
                )
        }
)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // OSM ID
    // =====================================================

    @Column(
            name = "osm_id",
            unique = true
    )
    private Long osmId;

    // =====================================================
    // RESTAURANT NAME
    // =====================================================

    @Column(
            nullable = false,
            length = 255
    )
    private String name;

    // =====================================================
    // TYPE
    // =====================================================

    @Column(
            nullable = false,
            length = 100
    )
    private String type;

    // =====================================================
    // CATEGORY
    // =====================================================

    @Column(
            length = 150
    )
    private String category;

    // =====================================================
    // CUISINE
    // =====================================================

    @Column(
            length = 255
    )
    private String cuisine;

    // =====================================================
    // ADDRESS
    // =====================================================

    @Column(
            length = 500
    )
    private String address;

    // =====================================================
    // PHONE
    // =====================================================

    @Column(
            length = 50
    )
    private String phone;

    // =====================================================
    // WEBSITE
    // =====================================================

    @Column(
            length = 500
    )
    private String website;

    // =====================================================
    // LATITUDE
    // =====================================================

    @Column(
            nullable = false
    )
    private Double latitude;

    // =====================================================
    // LONGITUDE
    // =====================================================

    @Column(
            nullable = false
    )
    private Double longitude;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Place() {
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOsmId() {
        return osmId;
    }

    public void setOsmId(Long osmId) {
        this.osmId = osmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}