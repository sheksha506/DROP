import React, { useEffect, useState } from "react";

import { useNavigate } from "react-router-dom";

import {
    getReviewsByPlace
} from "../../services/reviewService";

const RestaurantCard = ({ restaurant }) => {

    const navigate = useNavigate();

    const [reviews, setReviews] = useState([]);

    // --------------------------------------------------
    // Get a consistent restaurant/place ID
    // --------------------------------------------------

    const placeId =
        restaurant?.osmId ??
        restaurant?.id;

    // --------------------------------------------------
    // Load reviews
    // --------------------------------------------------

    useEffect(() => {

        let cancelled = false;

        const loadReviews = async () => {

            try {

                const data = await getReviewsByPlace(
                    placeId
                );

                if (!cancelled) {
                    setReviews(data);
                }

            } catch {

                if (!cancelled) {
                    setReviews([]);
                }

            }
        };

        if (placeId) {
            loadReviews();
        }

        return () => {
            cancelled = true;
        };

    }, [placeId]);

    // --------------------------------------------------
    // Rating
    // --------------------------------------------------

    const rating =
        reviews.length > 0
            ? reviews.reduce(
                (sum, review) =>
                    sum + Number(review.rating || 0),
                0
            ) / reviews.length
            : 0;

    const roundedRating =
        Math.round(rating * 10) / 10;

    // --------------------------------------------------
    // Distance
    // --------------------------------------------------

    const rawDistance =
        restaurant?.distance ??
        restaurant?.distanceKm ??
        restaurant?.distance_km ??
        restaurant?.distanceInKm;

    const distance = Number(rawDistance);

    const formattedDistance =
        Number.isFinite(distance)
            ? `${distance.toFixed(2)} km away`
            : "Distance unavailable";

    // --------------------------------------------------
    // Address
    // --------------------------------------------------

    const address =
        restaurant?.address ||
        restaurant?.display_name ||
        restaurant?.displayName ||
        restaurant?.formattedAddress ||
        restaurant?.formatted_address ||
        restaurant?.vicinity ||
        restaurant?.location?.address ||
        "Address not available";

    // --------------------------------------------------
    // Category
    // --------------------------------------------------

    const category =
        restaurant?.category ||
        restaurant?.type ||
        restaurant?.cuisine ||
        "Restaurant";

    // --------------------------------------------------
    // Restaurant name
    // --------------------------------------------------

    const restaurantName =
        restaurant?.name ||
        restaurant?.display_name ||
        "Restaurant";

    // --------------------------------------------------
    // View restaurant
    // --------------------------------------------------

    const handleViewRestaurant = () => {

        if (!placeId) {
            return;
        }

        navigate(`/restaurant/${placeId}`, {
            state: {
                restaurant,
                openReview: false
            }
        });
    };

    // --------------------------------------------------
    // Write review
    // --------------------------------------------------

    const handleWriteReview = () => {

        if (!placeId) {
            return;
        }

        navigate(`/restaurant/${placeId}`, {
            state: {
                restaurant,
                openReview: true
            }
        });
    };

    // --------------------------------------------------
    // View reviews
    // --------------------------------------------------

    const handleViewReviews = () => {

        if (!placeId) {
            return;
        }

        navigate(`/restaurant/${placeId}/reviews`, {
            state: {
                restaurant
            }
        });
    };

    // --------------------------------------------------
    // UI
    // --------------------------------------------------

    return (

        <div className="restaurant-card">

            <div className="restaurant-card-content">

                <div className="restaurant-card-category">
                    🍽️ {category}
                </div>

                <h2 className="restaurant-card-name">
                    {restaurantName}
                </h2>

                <p className="restaurant-card-address">
                    📍 {address}
                </p>

                <p className="restaurant-card-distance">
                    🚗 {formattedDistance}
                </p>

                <div className="restaurant-card-rating">

                    <span className="rating-stars">

                        {"★".repeat(
                            Math.round(roundedRating)
                        )}

                        <span className="empty-stars">

                            {"★".repeat(
                                5 -
                                Math.round(roundedRating)
                            )}

                        </span>

                    </span>

                    <span className="rating-number">

                        {reviews.length > 0
                            ? roundedRating.toFixed(1)
                            : "New"}

                    </span>

                    <button
                        type="button"
                        className="reviews-link"
                        onClick={handleViewReviews}
                    >
                        💬 {reviews.length}{" "}

                        {reviews.length === 1
                            ? "Review"
                            : "Reviews"}

                    </button>

                </div>

                <div className="restaurant-card-actions">

                    <button
                        type="button"
                        className="view-restaurant-button"
                        onClick={handleViewRestaurant}
                    >
                        View Restaurant
                    </button>

                    <button
                        type="button"
                        className="review-button"
                        onClick={handleWriteReview}
                    >
                        ⭐ Review
                    </button>

                </div>

            </div>

        </div>
    );
};

export default RestaurantCard;