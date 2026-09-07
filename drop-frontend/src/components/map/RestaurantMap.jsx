import React, { useEffect, useState } from "react";

import {
    MapContainer,
    TileLayer,
    Marker,
    Popup,
    Polyline,
    useMap
} from "react-leaflet";

import L from "leaflet";
import "leaflet/dist/leaflet.css";


// ==========================================
// FIX LEAFLET MARKER ICON
// ==========================================

delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
    iconRetinaUrl:
        "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",

    iconUrl:
        "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",

    shadowUrl:
        "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png"
});


// ==========================================
// GET LATITUDE
// ==========================================

const getLatitude = (restaurant) => {

    const value =
        restaurant?.lat ??
        restaurant?.latitude ??
        restaurant?.location?.lat ??
        restaurant?.location?.latitude ??
        restaurant?.coordinates?.lat ??
        restaurant?.coordinates?.latitude ??
        restaurant?.geometry?.location?.lat;

    const number = Number(value);

    return Number.isFinite(number)
        ? number
        : null;
};


// ==========================================
// GET LONGITUDE
// ==========================================

const getLongitude = (restaurant) => {

    const value =
        restaurant?.lon ??
        restaurant?.lng ??
        restaurant?.longitude ??
        restaurant?.location?.lon ??
        restaurant?.location?.lng ??
        restaurant?.location?.longitude ??
        restaurant?.coordinates?.lon ??
        restaurant?.coordinates?.lng ??
        restaurant?.coordinates?.longitude ??
        restaurant?.geometry?.location?.lng;

    const number = Number(value);

    return Number.isFinite(number)
        ? number
        : null;
};


// ==========================================
// MAP BOUNDS
// ==========================================

const MapBounds = ({
    userLocation,
    restaurantLocation,
    route
}) => {

    const map = useMap();

    useEffect(() => {

        const points = [];


        // USER LOCATION
        if (userLocation) {

            const lat =
                Number(
                    userLocation.latitude
                );

            const lon =
                Number(
                    userLocation.longitude
                );

            if (
                Number.isFinite(lat) &&
                Number.isFinite(lon)
            ) {

                points.push([
                    lat,
                    lon
                ]);

            }

        }


        // RESTAURANT
        if (restaurantLocation) {

            points.push([
                restaurantLocation.latitude,
                restaurantLocation.longitude
            ]);

        }


        // ROUTE
        if (
            Array.isArray(route) &&
            route.length > 0
        ) {

            route.forEach((point) => {

                const lat =
                    Number(
                        point?.latitude ??
                        point?.lat
                    );

                const lon =
                    Number(
                        point?.longitude ??
                        point?.lon ??
                        point?.lng
                    );

                if (
                    Number.isFinite(lat) &&
                    Number.isFinite(lon)
                ) {

                    points.push([
                        lat,
                        lon
                    ]);

                }

            });

        }


        // FIT MAP
        if (points.length >= 2) {

            map.fitBounds(
                points,
                {
                    padding: [40, 40]
                }
            );

        } else if (points.length === 1) {

            map.setView(
                points[0],
                16
            );

        }

    }, [
        map,
        userLocation,
        restaurantLocation,
        route
    ]);

    return null;
};


// ==========================================
// RESTAURANT MAP
// ==========================================

const RestaurantMap = ({
    restaurant
}) => {

    const [
        restaurantLocation,
        setRestaurantLocation
    ] = useState(null);


    const [
        restaurantAddress,
        setRestaurantAddress
    ] = useState(
        restaurant?.address ||
        null
    );


    const [
        searchingRestaurant,
        setSearchingRestaurant
    ] = useState(false);


    const [
        mapError,
        setMapError
    ] = useState(null);


    const [
        userLocation,
        setUserLocation
    ] = useState(null);


    const [
        route,
        setRoute
    ] = useState([]);


    const [
        routeDistance,
        setRouteDistance
    ] = useState(null);


    const [
        routeDuration,
        setRouteDuration
    ] = useState(null);


    const [
        loadingRoute,
        setLoadingRoute
    ] = useState(false);


    const [
        locationError,
        setLocationError
    ] = useState(null);


    const [
        routeError,
        setRouteError
    ] = useState(null);


    // ==========================================
    // FIND RESTAURANT
    // ==========================================

    useEffect(() => {

        if (!restaurant) {
            return;
        }


        const existingLatitude =
            getLatitude(restaurant);


        const existingLongitude =
            getLongitude(restaurant);


        // ======================================
        // COORDINATES ALREADY EXIST
        // ======================================

        if (
            existingLatitude !== null &&
            existingLongitude !== null
        ) {

            setRestaurantLocation({

                latitude:
                    existingLatitude,

                longitude:
                    existingLongitude

            });


            setRestaurantAddress(
                restaurant?.address ||
                restaurant?.display_name ||
                restaurant?.formatted_address ||
                null
            );


            return;
        }


        // ======================================
        // COORDINATES MISSING
        // SEARCH OPENSTREETMAP
        // ======================================

        const searchRestaurant =
            async () => {

                setSearchingRestaurant(true);
                setMapError(null);


                try {

                    const restaurantName =
                        restaurant?.name ||
                        restaurant?.display_name;


                    if (!restaurantName) {

                        throw new Error(
                            "Restaurant name is missing."
                        );

                    }


                    const query =
                        `${restaurantName}, Bangalore, India`;


                    const url =
                        `https://nominatim.openstreetmap.org/search` +
                        `?format=json` +
                        `&limit=1` +
                        `&q=${encodeURIComponent(query)}`;


                    const response =
                        await fetch(
                            url,
                            {
                                headers: {
                                    "Accept":
                                        "application/json"
                                }
                            }
                        );


                    if (!response.ok) {

                        throw new Error(
                            "OpenStreetMap search failed."
                        );

                    }


                    const results =
                        await response.json();


                    if (
                        !Array.isArray(results) ||
                        results.length === 0
                    ) {

                        throw new Error(
                            "Restaurant could not be found on OpenStreetMap."
                        );

                    }


                    const result =
                        results[0];


                    const latitude =
                        Number(result.lat);


                    const longitude =
                        Number(result.lon);


                    if (
                        !Number.isFinite(latitude) ||
                        !Number.isFinite(longitude)
                    ) {

                        throw new Error(
                            "OpenStreetMap returned invalid coordinates."
                        );

                    }


                    setRestaurantLocation({

                        latitude,
                        longitude

                    });


                    setRestaurantAddress(

                        restaurant?.address ||
                        result.display_name ||
                        "Address not available"

                    );


                } catch (error) {

                    console.error(
                        "Restaurant location error:",
                        error
                    );


                    setMapError(
                        error.message ||
                        "Unable to find restaurant location."
                    );


                } finally {

                    setSearchingRestaurant(false);

                }

            };


        searchRestaurant();

    }, [restaurant]);


    // ==========================================
    // GET USER LOCATION
    // ==========================================

    useEffect(() => {

        if (!navigator.geolocation) {

            setLocationError(
                "Your browser does not support location."
            );

            return;

        }


        navigator.geolocation.getCurrentPosition(

            (position) => {

                setUserLocation({

                    latitude:
                        position.coords.latitude,

                    longitude:
                        position.coords.longitude

                });

                setLocationError(null);

            },


            (error) => {

                console.error(
                    "Location error:",
                    error
                );


                setLocationError(
                    "Unable to get your location."
                );

            },


            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }

        );

    }, []);


    // ==========================================
    // GET ROUTE
    // ==========================================

    useEffect(() => {

        if (
            !userLocation ||
            !restaurantLocation
        ) {

            return;

        }


        const getRoute =
            async () => {

                setLoadingRoute(true);
                setRouteError(null);


                try {

                    const url =
                        `http://localhost:8081/api/routes` +
                        `?startLat=${userLocation.latitude}` +
                        `&startLon=${userLocation.longitude}` +
                        `&endLat=${restaurantLocation.latitude}` +
                        `&endLon=${restaurantLocation.longitude}` +
                        `&mode=drive`;


                    const response =
                        await fetch(url);


                    if (!response.ok) {

                        throw new Error(
                            `Route request failed (${response.status})`
                        );

                    }


                    const data =
                        await response.json();


                    setRoute(

                        Array.isArray(
                            data?.route
                        )
                            ? data.route
                            : []

                    );


                    setRouteDistance(
                        data?.distance ??
                        null
                    );


                    setRouteDuration(
                        data?.duration ??
                        null
                    );


                } catch (error) {

                    console.error(
                        "Route error:",
                        error
                    );


                    setRoute([]);

                    setRouteDistance(null);

                    setRouteDuration(null);


                    setRouteError(
                        "Unable to calculate driving route."
                    );


                } finally {

                    setLoadingRoute(false);

                }

            };


        getRoute();

    }, [
        userLocation,
        restaurantLocation
    ]);


    // ==========================================
    // NO RESTAURANT
    // ==========================================

    if (!restaurant) {
        return null;
    }


    // ==========================================
    // SEARCHING FOR RESTAURANT
    // ==========================================

    if (
        searchingRestaurant &&
        !restaurantLocation
    ) {

        return (

            <div
                className="restaurant-map-wrapper"
                style={{
                    minHeight: "450px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    background: "#f5f5f5",
                    borderRadius: "16px"
                }}
            >

                <div
                    style={{
                        textAlign: "center"
                    }}
                >

                    <div
                        style={{
                            fontSize: "40px",
                            marginBottom: "15px"
                        }}
                    >
                        📍
                    </div>


                    <h3>
                        Finding restaurant location...
                    </h3>


                    <p>
                        Searching OpenStreetMap
                    </p>

                </div>

            </div>

        );

    }


    // ==========================================
    // MAP ERROR
    // ==========================================

    if (
        mapError &&
        !restaurantLocation
    ) {

        return (

            <div
                className="restaurant-map-wrapper"
                style={{
                    minHeight: "450px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    background: "#f5f5f5",
                    borderRadius: "16px"
                }}
            >

                <div
                    style={{
                        textAlign: "center",
                        padding: "30px"
                    }}
                >

                    <div
                        style={{
                            fontSize: "40px"
                        }}
                    >
                        📍
                    </div>


                    <h3>
                        Restaurant location unavailable
                    </h3>


                    <p>
                        {mapError}
                    </p>


                    <p
                        style={{
                            fontSize: "13px",
                            opacity: 0.6
                        }}
                    >
                        Restaurant ID:{" "}
                        {restaurant.id}
                    </p>

                </div>

            </div>

        );

    }


    // ==========================================
    // STILL WAITING
    // ==========================================

    if (!restaurantLocation) {

        return (

            <div
                className="restaurant-map-wrapper"
                style={{
                    minHeight: "450px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    background: "#f5f5f5",
                    borderRadius: "16px"
                }}
            >

                <h3>
                    Loading map...
                </h3>

            </div>

        );

    }


    // ==========================================
    // RESTAURANT POSITION
    // ==========================================

    const restaurantPosition = [

        restaurantLocation.latitude,

        restaurantLocation.longitude

    ];


    // ==========================================
    // ROUTE POSITIONS
    // ==========================================

    const routePositions =

        route
            .map((point) => {

                const latitude =
                    Number(
                        point?.latitude ??
                        point?.lat
                    );


                const longitude =
                    Number(
                        point?.longitude ??
                        point?.lon ??
                        point?.lng
                    );


                if (
                    !Number.isFinite(
                        latitude
                    ) ||
                    !Number.isFinite(
                        longitude
                    )
                ) {

                    return null;

                }


                return [
                    latitude,
                    longitude
                ];

            })
            .filter(Boolean);


    // ==========================================
    // DISTANCE
    // ==========================================

    const distanceKm =

        routeDistance !== null

            ? (
                Number(routeDistance) / 1000
            ).toFixed(2)

            : null;


    // ==========================================
    // DURATION
    // ==========================================

    const durationMinutes =

        routeDuration !== null

            ? Math.ceil(
                Number(routeDuration) / 60
            )

            : null;


    // ==========================================
    // MAP
    // ==========================================

    return (

        <div className="restaurant-map-wrapper">


            {/* LOCATION ERROR */}

            {locationError && (

                <div className="map-location-error">

                    {locationError}

                </div>

            )}


            {/* RESTAURANT ADDRESS */}

            {restaurantAddress && (

                <div
                    style={{
                        padding:
                            "10px 0",
                        fontSize:
                            "14px"
                    }}
                >

                    📍 {restaurantAddress}

                </div>

            )}


            {/* ROUTE LOADING */}

            {loadingRoute && (

                <div className="route-loading">

                    🚗 Calculating driving route...

                </div>

            )}


            {/* ROUTE ERROR */}

            {routeError && (

                <div className="route-error">

                    {routeError}

                </div>

            )}


            {/* ROUTE INFO */}

            {!loadingRoute &&
                !routeError &&
                routePositions.length > 0 && (

                    <div className="route-info">

                        <span>
                            🚗 {distanceKm} km
                        </span>

                        <span>
                            ⏱️ {durationMinutes} min
                        </span>

                    </div>

                )}


            {/* ==================================
                OPENSTREETMAP
            ================================== */}

            <MapContainer

                center={
                    restaurantPosition
                }

                zoom={16}

                scrollWheelZoom={true}

                style={{
                    height: "450px",
                    width: "100%",
                    borderRadius: "16px"
                }}

            >

                <TileLayer

                    attribution="&copy; OpenStreetMap contributors"

                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"

                />


                {/* USER MARKER */}

                {userLocation && (

                    <Marker

                        position={[

                            userLocation.latitude,

                            userLocation.longitude

                        ]}

                    >

                        <Popup>

                            📍{" "}

                            <strong>
                                You are here
                            </strong>

                        </Popup>

                    </Marker>

                )}


                {/* RESTAURANT MARKER */}

                <Marker

                    position={
                        restaurantPosition
                    }

                >

                    <Popup>

                        <strong>

                            {
                                restaurant?.name ||
                                restaurant?.display_name ||
                                "Restaurant"
                            }

                        </strong>

                        <br />

                        {
                            restaurantAddress ||
                            "Restaurant location"
                        }

                    </Popup>

                </Marker>


                {/* ROAD ROUTE */}

                {routePositions.length > 0 && (

                    <Polyline

                        positions={
                            routePositions
                        }

                        pathOptions={{
                            weight: 6
                        }}

                    />

                )}


                {/* FIT MAP */}

                <MapBounds

                    userLocation={
                        userLocation
                    }

                    restaurantLocation={
                        restaurantLocation
                    }

                    route={
                        route
                    }

                />

            </MapContainer>

        </div>

    );

};


export default RestaurantMap;