import React, {
    useEffect,
    useMemo,
    useRef,
    useState
} from "react";

import Navbar
    from "../components/layout/Navbar";

import RestaurantCard
    from "../components/restaurant/RestaurantCard";

import {
    getNearbyRestaurants
} from "../services/osmService";

import {
    useAuth
} from "../context/AuthContext";


const API_BASE_URL = "http://localhost:8081/api/places";


const Home = () => {

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    const {
        user,
        loading: authLoading
    } = useAuth();


    const isLoggedIn = !!user;


    // ==========================================
    // SEARCH
    // ==========================================

    const [
        searchQuery,
        setSearchQuery
    ] = useState("");


    // ==========================================
    // LOCATION STATUS
    // ==========================================

    const [
        locationStatus,
        setLocationStatus
    ] = useState("checking");


    // ==========================================
    // USER LOCATION
    // ==========================================

    const [
        userLocation,
        setUserLocation
    ] = useState(null);


    // ==========================================
    // RESTAURANTS
    // ==========================================

    const [
        restaurants,
        setRestaurants
    ] = useState([]);


    // ==========================================
    // LOADING
    // ==========================================

    const [
        loadingRestaurants,
        setLoadingRestaurants
    ] = useState(false);


    // ==========================================
    // ERROR
    // ==========================================

    const [
        restaurantError,
        setRestaurantError
    ] = useState("");


    // ==========================================
    // FRONTEND PAGINATION
    // ==========================================

    const RESTAURANTS_PER_PAGE = 12;

    const [
        visibleRestaurantCount,
        setVisibleRestaurantCount
    ] = useState(
        RESTAURANTS_PER_PAGE
    );

    const loadMoreRef =
        useRef(null);


    // ==========================================
    // PREVENT DUPLICATE REQUESTS
    // ==========================================

    const restaurantsLoadedRef =
        useRef(false);


    // ==========================================
    // LOAD NEARBY RESTAURANTS
    // ==========================================

    const loadNearbyRestaurants = async (
        latitude,
        longitude
    ) => {

        // --------------------------------------
        // Prevent duplicate requests
        // --------------------------------------

        if (restaurantsLoadedRef.current) {
            return;
        }


        restaurantsLoadedRef.current = true;


        setLoadingRestaurants(true);
        setRestaurantError("");


        try {

            const data =
                await getNearbyRestaurants(
                    latitude,
                    longitude,
                    5000
                );


            const restaurantList =
                Array.isArray(data)
                    ? data
                    : [];


            setRestaurants(
                restaurantList
            );


            // ==================================
            // SAVE RESTAURANTS FOR SESSION
            // ==================================

            sessionStorage.setItem(
                "dropRestaurants",
                JSON.stringify(
                    restaurantList
                )
            );


            sessionStorage.setItem(
                "dropRestaurantsLatitude",
                String(latitude)
            );


            sessionStorage.setItem(
                "dropRestaurantsLongitude",
                String(longitude)
            );


        } catch (error) {

            // Allow another attempt
            // if request failed.

            restaurantsLoadedRef.current =
                false;


            setRestaurantError(
                error?.message ||
                "Unable to load nearby restaurants."
            );


            setRestaurants([]);


        } finally {

            setLoadingRestaurants(false);

        }

    };


    // ==========================================
    // LOAD RANDOM DATABASE RESTAURANTS
    // ==========================================

    const loadRandomRestaurants = async () => {

        setLoadingRestaurants(true);
        setRestaurantError("");


        try {

            const response =
                await fetch(
                    `${API_BASE_URL}/random`
                );


            if (!response.ok) {

                throw new Error(
                    `Unable to load restaurants (${response.status})`
                );

            }


            const data =
                await response.json();


            const restaurantList =
                Array.isArray(data)
                    ? data
                    : [];


            setRestaurants(
                restaurantList
            );


        } catch (error) {

            setRestaurantError(
                error?.message ||
                "Unable to load restaurants."
            );


            setRestaurants([]);


        } finally {

            setLoadingRestaurants(false);

        }

    };


    // ==========================================
    // REQUEST LOCATION
    // ==========================================

    const requestLocation = () => {

        // --------------------------------------
        // Safety:
        // NEVER request location while logged out
        // --------------------------------------

        if (!isLoggedIn) {
            return;
        }


        if (!navigator.geolocation) {

            setLocationStatus(
                "unavailable"
            );

            return;

        }


        setLocationStatus(
            "checking"
        );


        navigator.geolocation.getCurrentPosition(

            // ==================================
            // SUCCESS
            // ==================================

            async (position) => {

                const latitude =
                    position.coords.latitude;


                const longitude =
                    position.coords.longitude;


                // ==================================
                // SAVE APPROVED LOCATION
                // ==================================

                localStorage.setItem(
                    "dropLocationGranted",
                    "true"
                );


                localStorage.setItem(
                    "dropLatitude",
                    String(latitude)
                );


                localStorage.setItem(
                    "dropLongitude",
                    String(longitude)
                );


                // ==================================
                // SET LOCATION
                // ==================================

                setUserLocation({
                    latitude,
                    longitude
                });


                setLocationStatus(
                    "accepted"
                );


                // ==================================
                // LOAD NEARBY RESTAURANTS
                // ==================================

                await loadNearbyRestaurants(
                    latitude,
                    longitude
                );

            },


            // ==================================
            // ERROR
            // ==================================

            (error) => {

                if (
                    error.code ===
                    error.PERMISSION_DENIED
                ) {

                    // --------------------------------
                    // User denied location
                    // --------------------------------

                    localStorage.removeItem(
                        "dropLocationGranted"
                    );


                    localStorage.removeItem(
                        "dropLatitude"
                    );


                    localStorage.removeItem(
                        "dropLongitude"
                    );


                    sessionStorage.removeItem(
                        "dropRestaurants"
                    );


                    sessionStorage.removeItem(
                        "dropRestaurantsLatitude"
                    );


                    sessionStorage.removeItem(
                        "dropRestaurantsLongitude"
                    );


                    setLocationStatus(
                        "denied"
                    );


                } else {

                    setLocationStatus(
                        "error"
                    );

                }

            },


            {
                enableHighAccuracy: true,
                timeout: 30000,
                maximumAge: 0
            }

        );

    };


    // ==========================================
    // INITIAL HOME LOAD
    // ==========================================

    useEffect(() => {

        // ======================================
        // WAIT FOR AUTH TO FINISH
        // ======================================

        if (authLoading) {
            return;
        }


        // ======================================
        // LOGGED OUT
        // ======================================

        if (!isLoggedIn) {

            // ----------------------------------
            // IMPORTANT:
            // NEVER use browser location here.
            // ----------------------------------

            setLocationStatus(
                "logged-out"
            );


            setUserLocation(null);


            // ----------------------------------
            // Clear old nearby restaurant data
            // ----------------------------------

            sessionStorage.removeItem(
                "dropRestaurants"
            );


            sessionStorage.removeItem(
                "dropRestaurantsLatitude"
            );


            sessionStorage.removeItem(
                "dropRestaurantsLongitude"
            );


            // ----------------------------------
            // Load random restaurants
            // from our database.
            // ----------------------------------

            loadRandomRestaurants();


            return;

        }


        // ======================================
        // LOGGED IN
        // ======================================

        // Reset request protection because
        // authentication state has changed.

        restaurantsLoadedRef.current =
            false;


        // ======================================
        // FIRST:
        // CHECK SAVED NEARBY RESTAURANTS
        // ======================================

        const savedRestaurants =
            sessionStorage.getItem(
                "dropRestaurants"
            );


        const savedLatitude =
            Number(
                sessionStorage.getItem(
                    "dropRestaurantsLatitude"
                )
            );


        const savedLongitude =
            Number(
                sessionStorage.getItem(
                    "dropRestaurantsLongitude"
                )
            );


        if (
            savedRestaurants &&
            Number.isFinite(savedLatitude) &&
            Number.isFinite(savedLongitude)
        ) {

            try {

                const parsedRestaurants =
                    JSON.parse(
                        savedRestaurants
                    );


                if (
                    Array.isArray(
                        parsedRestaurants
                    )
                  )
                {

                    setRestaurants(
                        parsedRestaurants
                    );


                    setUserLocation({
                        latitude:
                            savedLatitude,

                        longitude:
                            savedLongitude
                    });


                    setLocationStatus(
                        "accepted"
                    );


                    restaurantsLoadedRef.current =
                        true;


                    return;

                }

            } catch {
                // Continue to saved location check.
            }

        }


        // ======================================
        // SECOND:
        // CHECK SAVED LOCATION
        // ======================================

        const locationGranted =
            localStorage.getItem(
                "dropLocationGranted"
            );


        const latitude =
            Number(
                localStorage.getItem(
                    "dropLatitude"
                )
            );


        const longitude =
            Number(
                localStorage.getItem(
                    "dropLongitude"
                )
            );


        // ======================================
        // LOCATION ALREADY APPROVED
        // ======================================

        if (
            locationGranted === "true" &&
            Number.isFinite(latitude) &&
            Number.isFinite(longitude)
        ) {

            setUserLocation({
                latitude,
                longitude
            });


            setLocationStatus(
                "accepted"
            );


            // Use saved location.
            // Do not ask browser again.

            loadNearbyRestaurants(
                latitude,
                longitude
            );


            return;

        }


        // ======================================
        // FIRST-TIME LOGGED-IN USER
        // ======================================

        setLocationStatus(
            "waiting"
        );

    }, [
        authLoading,
        isLoggedIn
    ]);


    // ==========================================
    // ALLOW LOCATION BUTTON
    // ==========================================

    const handleAllowLocation = () => {

        requestLocation();

    };


    // ==========================================
    // SEARCH FILTER
    // ==========================================

    const filteredRestaurants =
        useMemo(() => {

            const query =
                searchQuery
                    .trim()
                    .toLowerCase();


            // ----------------------------------
            // Empty search
            // ----------------------------------

            if (!query) {

                return restaurants;

            }


            // ----------------------------------
            // Filter restaurants
            // ----------------------------------

            return restaurants.filter(
                (restaurant) => {

                    const name =
                        restaurant?.name
                            ?.toLowerCase() ||
                        "";


                    const address =
                        restaurant?.address
                            ?.toLowerCase() ||
                        "";


                    const category =
                        restaurant?.category
                            ?.toLowerCase() ||
                        "";


                    const cuisine =
                        restaurant?.cuisine
                            ?.toLowerCase() ||
                        "";


                    return (
                        name.includes(query) ||
                        address.includes(query) ||
                        category.includes(query) ||
                        cuisine.includes(query)
                    );

                }
            );

        }, [
            searchQuery,
            restaurants
        ]);


    // ==========================================
    // RESET PAGINATION WHEN SEARCH CHANGES
    // ==========================================

    useEffect(() => {

        setVisibleRestaurantCount(
            RESTAURANTS_PER_PAGE
        );

    }, [searchQuery]);


    // ==========================================
    // INFINITE SCROLL
    // ==========================================

    useEffect(() => {

        const sentinel =
            loadMoreRef.current;

        if (!sentinel) {
            return;
        }

        if (
            visibleRestaurantCount >=
            filteredRestaurants.length
        ) {
            return;
        }

        const observer =
            new IntersectionObserver(
                (entries) => {

                    if (entries[0]?.isIntersecting) {

                        setVisibleRestaurantCount(
                            (currentCount) =>
                                Math.min(
                                    currentCount +
                                        RESTAURANTS_PER_PAGE,
                                    filteredRestaurants.length
                                )
                        );

                    }

                },
                {
                    rootMargin: "400px"
                }
            );

        observer.observe(sentinel);

        return () => {
            observer.disconnect();
        };

    }, [
        filteredRestaurants.length,
        visibleRestaurantCount
    ]);


    // ==========================================
    // VISIBLE RESTAURANTS
    // ==========================================

    const visibleRestaurants =
        useMemo(() => {

            return filteredRestaurants.slice(
                0,
                visibleRestaurantCount
            );

        }, [
            filteredRestaurants,
            visibleRestaurantCount
        ]);


    // ==========================================
    // AUTH LOADING
    // ==========================================

    if (authLoading) {

        return (
            <div className="home-page">

                <Navbar
                    searchQuery={searchQuery}
                    onSearch={setSearchQuery}
                />

                <div className="restaurants-empty">

                    <h2>
                        Loading DROP...
                    </h2>

                </div>

            </div>
        );

    }


    // ==========================================
    // RENDER
    // ==========================================

    return (

        <div className="home-page">

            {/* ==================================
                NAVBAR
            ================================== */}

            <Navbar
                searchQuery={
                    searchQuery
                }
                onSearch={
                    setSearchQuery
                }
            />


            {/* ==================================
                LOGGED OUT
            ================================== */}

            {!isLoggedIn && (

                <>

                    {/* ==================================
                        HERO
                    ================================== */}

                    <section
                        className="home-hero"
                    >

                        <div
                            className="home-location"
                        >

                            <span>
                                🍽️
                            </span>

                            <strong>
                                Welcome to DROP
                            </strong>

                        </div>


                        <h1>
                            Discover restaurants
                            <br />
                            worth trying.
                        </h1>


                        <p>
                            Explore restaurants
                            from the DROP community.
                        </p>

                    </section>


                    {/* ==================================
                        RANDOM RESTAURANTS
                    ================================== */}

                    <section
                        className="home-restaurants"
                    >

                        <div
                            className="home-restaurants-header"
                        >

                            <div>

                                <h2>
                                    Restaurants you
                                    might like
                                </h2>


                                <p>
                                    A selection of
                                    restaurants from DROP.
                                </p>


                                {!loadingRestaurants && (

                                    <span
                                        className=
                                            "home-restaurant-count"
                                    >

                                        {
                                            filteredRestaurants.length
                                        }{" "}

                                        {
                                            filteredRestaurants.length === 1
                                                ? "place"
                                                : "places"
                                        }

                                    </span>

                                )}

                            </div>

                        </div>


                        {/* ==================================
                            LOADING
                        ================================== */}

                        {loadingRestaurants && (

                            <div
                                className=
                                    "restaurants-empty"
                            >

                                <h2>
                                    Discovering restaurants...
                                </h2>


                                <p>
                                    Finding some restaurants
                                    from DROP.
                                </p>

                            </div>

                        )}


                        {/* ==================================
                            ERROR
                        ================================== */}

                        {!loadingRestaurants &&
                            restaurantError && (

                            <div
                                className=
                                    "restaurants-empty"
                            >

                                <h2>
                                    Unable to load
                                    restaurants
                                </h2>


                                <p>
                                    {restaurantError}
                                </p>

                            </div>

                        )}


                        {/* ==================================
                            RESTAURANT GRID
                        ================================== */}

                        {!loadingRestaurants &&
                            !restaurantError &&
                            filteredRestaurants.length > 0 && (

                            <>

                            <div
                                className=
                                    "restaurants-grid"
                            >

                                {
                                    visibleRestaurants.map(
                                        (restaurant) => (

                                            <RestaurantCard
                                                key={
                                                    restaurant?.osmId ||
                                                    restaurant?.id ||
                                                    `${restaurant?.name}-${restaurant?.latitude}`
                                                }
                                                restaurant={
                                                    restaurant
                                                }
                                            />

                                        )
                                    )
                                }

                            </div>

                            {visibleRestaurants.length <
                                filteredRestaurants.length && (
                                <div
                                    ref={loadMoreRef}
                                    aria-hidden="true"
                                    style={{
                                        height: "1px"
                                    }}
                                />
                            )}


                            </>

                        )}


                        {/* ==================================
                            EMPTY
                        ================================== */}

                        {!loadingRestaurants &&
                            !restaurantError &&
                            filteredRestaurants.length === 0 && (

                            <div
                                className=
                                    "restaurants-empty"
                            >

                                <h2>
                                    No restaurants found
                                </h2>


                                <p>
                                    No restaurants are
                                    currently available.
                                </p>

                            </div>

                        )}

                    </section>

                </>

            )}


            {/* ==================================
                LOGGED IN
            ================================== */}

            {isLoggedIn && (

                <>

                    {/* ==================================
                        LOCATION REQUEST
                    ================================== */}

                    {locationStatus !== "accepted" && (

                        <section
                            className="location-request"
                        >

                            <div
                                className=
                                    "location-request-content"
                            >

                                <div
                                    className=
                                        "location-request-icon"
                                >
                                    📍
                                </div>


                                <h1>
                                    Find restaurants near you
                                </h1>


                                <p>
                                    Allow DROP to access
                                    your location so we
                                    can show restaurants
                                    around you.
                                </p>


                                {/* ==================================
                                    FIRST TIME
                                ================================== */}

                                {locationStatus ===
                                    "waiting" && (

                                    <button
                                        type="button"
                                        className=
                                            "location-allow-button"
                                        onClick={
                                            handleAllowLocation
                                        }
                                    >
                                        Allow Location
                                    </button>

                                )}


                                {/* ==================================
                                    CHECKING
                                ================================== */}

                                {locationStatus ===
                                    "checking" && (

                                    <div
                                        className=
                                            "location-loading"
                                    >
                                        📍 Getting your
                                        location...
                                    </div>

                                )}


                                {/* ==================================
                                    DENIED
                                ================================== */}

                                {locationStatus ===
                                    "denied" && (

                                    <>

                                        <div
                                            className=
                                                "location-error"
                                        >

                                            Location access
                                            was denied.

                                            <br />

                                            Please allow
                                            location access
                                            in your browser
                                            settings.

                                        </div>


                                        <button
                                            type="button"
                                            className=
                                                "location-allow-button"
                                            onClick={
                                                handleAllowLocation
                                            }
                                        >
                                            Try Again
                                        </button>

                                    </>

                                )}


                                {/* ==================================
                                    ERROR
                                ================================== */}

                                {locationStatus ===
                                    "error" && (

                                    <>

                                        <div
                                            className=
                                                "location-error"
                                        >

                                            Unable to get
                                            your location.

                                        </div>


                                        <button
                                            type="button"
                                            className=
                                                "location-allow-button"
                                            onClick={
                                                handleAllowLocation
                                            }
                                        >
                                            Try Again
                                        </button>

                                    </>

                                )}


                                {/* ==================================
                                    UNSUPPORTED
                                ================================== */}

                                {locationStatus ===
                                    "unavailable" && (

                                    <div
                                        className=
                                            "location-error"
                                    >

                                        Your browser does
                                        not support location
                                        access.

                                    </div>

                                )}

                            </div>

                        </section>

                    )}


                    {/* ==================================
                        NEARBY RESTAURANTS
                    ================================== */}

                    {locationStatus === "accepted" && (

                        <>

                            {/* ==================================
                                HERO
                            ================================== */}

                            <section
                                className="home-hero"
                            >

                                <div
                                    className="home-location"
                                >

                                    <span>
                                        📍
                                    </span>


                                    <strong>
                                        Restaurants near you
                                    </strong>

                                </div>


                                <h1>
                                    Discover restaurants
                                    <br />
                                    around you.
                                </h1>


                                <p>
                                    Real restaurants from
                                    OpenStreetMap based on
                                    your approved location.
                                </p>

                            </section>


                            {/* ==================================
                                RESTAURANTS
                            ================================== */}

                            <section
                                className="home-restaurants"
                            >

                                <div
                                    className=
                                        "home-restaurants-header"
                                >

                                    <div>

                                        <h2>
                                            Restaurants near you
                                        </h2>


                                        <p>
                                            Real restaurants
                                            within 5 km of
                                            your location.
                                        </p>


                                        {!loadingRestaurants && (

                                            <span
                                                className=
                                                    "home-restaurant-count"
                                            >

                                                {
                                                    filteredRestaurants.length
                                                }{" "}

                                                {
                                                    filteredRestaurants.length === 1
                                                        ? "place"
                                                        : "places"
                                                }

                                            </span>

                                        )}

                                    </div>

                                </div>


                                {/* ==================================
                                    LOADING
                                ================================== */}

                                {loadingRestaurants && (

                                    <div
                                        className=
                                            "restaurants-empty"
                                    >

                                        <h2>
                                            Finding restaurants...
                                        </h2>


                                        <p>
                                            📍 Searching
                                            OpenStreetMap
                                            near your approved
                                            location...
                                        </p>

                                    </div>

                                )}


                                {/* ==================================
                                    ERROR
                                ================================== */}

                                {!loadingRestaurants &&
                                    restaurantError && (

                                    <div
                                        className=
                                            "restaurants-empty"
                                    >

                                        <h2>
                                            Unable to load
                                            restaurants
                                        </h2>


                                        <p>
                                            {restaurantError}
                                        </p>

                                    </div>

                                )}


                                {/* ==================================
                                    RESTAURANT GRID
                                ================================== */}

                                {!loadingRestaurants &&
                                    !restaurantError &&
                                    filteredRestaurants.length > 0 && (

                            <>

                                    <div
                                        className=
                                            "restaurants-grid"
                                    >

                                        {
                                            visibleRestaurants.map(
                                                (restaurant) => (

                                                    <RestaurantCard
                                                        key={
                                                            restaurant?.osmId ||
                                                            restaurant?.id ||
                                                            `${restaurant?.name}-${restaurant?.latitude}`
                                                        }
                                                        restaurant={
                                                            restaurant
                                                        }
                                                    />

                                                )
                                            )
                                        }

                                    </div>

                                    {visibleRestaurants.length <
                                        filteredRestaurants.length && (
                                        <div
                                            ref={loadMoreRef}
                                            aria-hidden="true"
                                            style={{
                                                height: "1px"
                                            }}
                                        />
                                    )}


                                    </>

                                )}


                                {/* ==================================
                                    EMPTY
                                ================================== */}

                                {!loadingRestaurants &&
                                    !restaurantError &&
                                    filteredRestaurants.length === 0 && (

                                    <div
                                        className=
                                            "restaurants-empty"
                                    >

                                        <h2>
                                            No restaurants found
                                        </h2>


                                        <p>
                                            No restaurants were
                                            found within 5 km.
                                        </p>

                                    </div>

                                )}

                            </section>

                        </>

                    )}

                </>

            )}

        </div>

    );

};


export default Home;