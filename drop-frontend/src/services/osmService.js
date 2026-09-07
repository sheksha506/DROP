import {
    dedupedRequest
} from "./requestDedup";


// ==========================================
// OPENSTREETMAP / OVERPASS SERVICE
// ==========================================


// ==========================================
// OVERPASS SERVERS
// ==========================================

const OVERPASS_SERVERS = [

    "https://overpass-api.de/api/interpreter",

    "https://overpass.kumi.systems/api/interpreter",

    "https://overpass.private.coffee/api/interpreter"

];


// ==========================================
// DISTANCE CALCULATION
// ==========================================

const calculateDistance = (
    userLat,
    userLon,
    restaurantLat,
    restaurantLon
) => {

    const toRadians = (value) =>
        value * Math.PI / 180;

    const earthRadius = 6371;

    const latDifference =
        toRadians(
            restaurantLat - userLat
        );

    const lonDifference =
        toRadians(
            restaurantLon - userLon
        );

    const a =
        Math.sin(latDifference / 2) *
        Math.sin(latDifference / 2) +

        Math.cos(
            toRadians(userLat)
        ) *

        Math.cos(
            toRadians(restaurantLat)
        ) *

        Math.sin(lonDifference / 2) *
        Math.sin(lonDifference / 2);

    const c =
        2 *
        Math.atan2(
            Math.sqrt(a),
            Math.sqrt(1 - a)
        );

    return (
        earthRadius * c
    );

};


// ==========================================
// BUILD ADDRESS
// ==========================================

const buildAddress = (tags) => {

    if (!tags) {

        return "Address not available";

    }

    const parts = [];


    if (
        tags["addr:housenumber"]
    ) {

        parts.push(
            tags["addr:housenumber"]
        );

    }


    if (
        tags["addr:street"]
    ) {

        parts.push(
            tags["addr:street"]
        );

    }


    if (
        tags["addr:suburb"]
    ) {

        parts.push(
            tags["addr:suburb"]
        );

    }


    if (
        tags["addr:city"]
    ) {

        parts.push(
            tags["addr:city"]
        );

    }


    if (
        tags["addr:postcode"]
    ) {

        parts.push(
            tags["addr:postcode"]
        );

    }


    if (parts.length > 0) {

        return parts.join(", ");

    }


    return (

        tags["addr:full"] ||

        tags["description"] ||

        "Address not available"

    );

};


// ==========================================
// CUISINE
// ==========================================

const getCuisine = (tags) => {

    if (!tags?.cuisine) {

        return "Restaurant";

    }

    return tags.cuisine

        .split(";")

        .map((item) =>

            item
                .trim()
                .replace(
                    /\b\w/g,
                    (letter) =>
                        letter.toUpperCase()
                )

        )

        .join(", ");

};


// ==========================================
// RUN OVERPASS QUERY
// ==========================================

const requestOverpass = async (
    server,
    query
) => {

    console.log(
        "Trying Overpass server:",
        server
    );


    const controller =
        new AbortController();


    const timeout =
        setTimeout(() => {

            controller.abort();

        }, 20000);


    try {

        const response =
            await fetch(
                server,
                {

                    method: "POST",

                    headers: {

                        "Content-Type":
                            "application/x-www-form-urlencoded"

                    },

                    body:
                        "data=" +
                        encodeURIComponent(
                            query
                        ),

                    signal:
                        controller.signal

                }
            );


        clearTimeout(
            timeout
        );


        if (!response.ok) {

            throw new Error(
                `Overpass server returned ${response.status}`
            );

        }


        return await response.json();

    } catch (error) {

        clearTimeout(
            timeout
        );

        throw error;

    }

};


// ==========================================
// GET NEARBY RESTAURANTS
// ==========================================

export const getNearbyRestaurants = (
    latitude,
    longitude,
    radius = 3000
) => {

    const lat =
        Number(latitude);

    const lon =
        Number(longitude);

    const safeRadius =
        Number(radius);


    // ======================================
    // VALIDATE COORDINATES
    // ======================================

    if (
        !Number.isFinite(lat) ||
        !Number.isFinite(lon)
    ) {

        return Promise.reject(
            new Error(
                "Invalid location coordinates."
            )
        );

    }


    // ======================================
    // VALIDATE RADIUS
    // ======================================

    if (
        !Number.isFinite(safeRadius) ||
        safeRadius <= 0
    ) {

        return Promise.reject(
            new Error(
                "Invalid search radius."
            )
        );

    }


    // ======================================
    // NORMALIZE REQUEST KEY
    // ======================================

    const requestKey =
        `nearby:${lat.toFixed(5)}:${lon.toFixed(5)}:${safeRadius}`;


    // ======================================
    // DEDUPLICATE REQUEST
    // ======================================

    return dedupedRequest(
        requestKey,
        async () => {

            console.log(
                "Searching OSM around:",
                lat,
                lon
            );


            // ==================================
            // LIGHTWEIGHT QUERY
            // ==================================

            const query = `
[out:json][timeout:15];

nwr[
    "amenity"="restaurant"
](
    around:${safeRadius},
    ${lat},
    ${lon}
);

out center tags;
`;


            let data = null;


            // ==================================
            // TRY SERVERS ONE BY ONE
            // ==================================

            for (
                const server
                of OVERPASS_SERVERS
            ) {

                try {

                    data =
                        await requestOverpass(
                            server,
                            query
                        );


                    if (
                        data &&
                        Array.isArray(
                            data.elements
                        )
                    ) {

                        console.log(
                            "Overpass success:",
                            server
                        );

                        break;

                    }

                } catch (error) {

                    console.warn(
                        "Overpass server failed:",
                        server,
                        error
                    );

                }

            }


            // ==================================
            // ALL SERVERS FAILED
            // ==================================

            if (
                !data ||
                !Array.isArray(
                    data.elements
                )
            ) {

                throw new Error(
                    "OpenStreetMap servers are currently unavailable. Please try again."
                );

            }


            // ==================================
            // CONVERT OSM DATA
            // ==================================

            const restaurants =
                data.elements

                    .map((element) => {

                        const tags =
                            element.tags || {};


                        // ==================================
                        // COORDINATES
                        // ==================================

                        const restaurantLatitude =
                            Number(
                                element.lat ??
                                element.center?.lat
                            );


                        const restaurantLongitude =
                            Number(
                                element.lon ??
                                element.center?.lon
                            );


                        if (
                            !Number.isFinite(
                                restaurantLatitude
                            ) ||
                            !Number.isFinite(
                                restaurantLongitude
                            )
                        ) {

                            return null;

                        }


                        // ==================================
                        // NAME
                        // ==================================

                        const name =
                            tags.name ||
                            tags["name:en"];


                        // ==================================
                        // IGNORE UNNAMED
                        // ==================================

                        if (!name) {

                            return null;

                        }


                        // ==================================
                        // DISTANCE
                        // ==================================

                        const distance =
                            calculateDistance(
                                lat,
                                lon,
                                restaurantLatitude,
                                restaurantLongitude
                            );


                        // ==================================
                        // DROP RESTAURANT OBJECT
                        // ==================================

                        return {

                            id:
                                Number(
                                    element.id
                                ),

                            osmId:
                                Number(
                                    element.id
                                ),

                            osmType:
                                element.type,

                            name:

                                name,

                            category:

                                getCuisine(
                                    tags
                                ),

                            cuisine:

                                tags.cuisine ||
                                null,

                            address:

                                buildAddress(
                                    tags
                                ),

                            // Coordinates

                            lat:
                                restaurantLatitude,

                            lon:
                                restaurantLongitude,

                            latitude:
                                restaurantLatitude,

                            longitude:
                                restaurantLongitude,

                            // Distance

                            distance:

                                distance,

                            // Extra data

                            phone:

                                tags.phone ||
                                tags["contact:phone"] ||
                                null,

                            website:

                                tags.website ||
                                tags["contact:website"] ||
                                null,

                            openingHours:

                                tags.opening_hours ||
                                null,

                            tags

                        };

                    })

                    .filter(Boolean);


            // ==================================
            // SORT BY DISTANCE
            // ==================================

            restaurants.sort(
                (a, b) =>
                    a.distance -
                    b.distance
            );


            // ==================================
            // REMOVE DUPLICATES
            // ==================================

            const uniqueRestaurants = [];

            const seen =
                new Set();


            for (
                const restaurant
                of restaurants
            ) {

                const key =
                    `${restaurant.name.toLowerCase()}-${restaurant.lat.toFixed(5)}-${restaurant.lon.toFixed(5)}`;


                if (
                    !seen.has(key)
                ) {

                    seen.add(key);

                    uniqueRestaurants.push(
                        restaurant
                    );

                }

            }


            console.log(
                `Found ${uniqueRestaurants.length} restaurants`
            );


            return uniqueRestaurants;

        }
    );

};


// ==========================================
// LEGACY COMPATIBILITY FUNCTION
// ==========================================

export const getRestaurantsNearby = async (
    latitude,
    longitude,
    radiusKm = 5
) => {

    const radiusMeters =
        Number(radiusKm) * 1000;

    return getNearbyRestaurants(
        latitude,
        longitude,
        radiusMeters
    );

};