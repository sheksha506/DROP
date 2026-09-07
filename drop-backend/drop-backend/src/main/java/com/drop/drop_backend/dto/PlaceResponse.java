package com.drop.drop_backend.dto;



public class PlaceResponse {

    private Long osmId;

    private String name;

    private String type;

    private String category;

    private String cuisine;

    private String address;

    private String phone;

    private String website;

    private Double latitude;

    private Double longitude;

    private Double distance;

    public PlaceResponse() {
    }

    public PlaceResponse(
            Long osmId,
            String name,
            String type,
            String category,
            String cuisine,
            String address,
            String phone,
            String website,
            Double latitude,
            Double longitude,
            Double distance
    ) {
        this.osmId = osmId;
        this.name = name;
        this.type = type;
        this.category = category;
        this.cuisine = cuisine;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
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

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
