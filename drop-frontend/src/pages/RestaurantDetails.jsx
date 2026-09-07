import React from "react";
import {
    useLocation,
    useNavigate
} from "react-router-dom";

import RestaurantMap
    from "../components/map/RestaurantMap";

import ReviewForm
    from "../components/reviews/ReviewForm";


const RestaurantDetails = () => {

    const location =
        useLocation();

    const navigate =
        useNavigate();


    const restaurant =
        location.state?.restaurant;


    const openReview =
        location.state?.openReview === true;


    if (!restaurant) {

        return (

            <div
                style={{
                    minHeight: "100vh",
                    padding: "40px"
                }}
            >

                <h2>
                    Restaurant not found
                </h2>

                <button
                    type="button"
                    onClick={() =>
                        navigate("/")
                    }
                >
                    ← Back to Home
                </button>

            </div>

        );

    }


    const distance =
        Number(
            restaurant?.distance
        );


    const formattedDistance =
        Number.isFinite(distance)
            ? `${distance.toFixed(2)} km away`
            : "Distance unavailable";


    const address =
        restaurant?.address ||
        restaurant?.display_name ||
        restaurant?.displayName ||
        restaurant?.formatted_address ||
        restaurant?.formattedAddress ||
        restaurant?.vicinity ||
        "Address not available";


    return (

        <div className="restaurant-details-page">


            {/* BACK */}

            <button
                type="button"
                className="back-button"
                onClick={() =>
                    navigate(-1)
                }
            >
                ← Back
            </button>


            {/* RESTAURANT HEADER */}

            <div className="restaurant-details-header">

                <div>

                    <span className="restaurant-details-category">

                        🍽️ Restaurant

                    </span>


                    <h1>

                        {
                            restaurant?.name ||
                            restaurant?.display_name ||
                            "Restaurant"
                        }

                    </h1>


                    <p className="restaurant-details-distance">

                        📍 {formattedDistance}

                    </p>


                    <p className="restaurant-details-address">

                        {address}

                    </p>

                </div>

            </div>


            {/* MAP */}

            {!openReview && (

                <div className="restaurant-details-map">

                    <h2>
                        Location & Directions
                    </h2>

                    <RestaurantMap
                        restaurant={restaurant}
                    />

                </div>

            )}


            {/* REVIEW */}

            {openReview && (

                <div
                    className="restaurant-reviews-section"
                    id="reviews"
                >

                    <ReviewForm
                        placeId={
                            restaurant.id
                        }
                    />

                </div>

            )}

        </div>

    );

};


export default RestaurantDetails;