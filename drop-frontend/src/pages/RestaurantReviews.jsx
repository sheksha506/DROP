import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getReviewsByPlace } from "../services/reviewService";

const RestaurantReviews = () => {
    const { id } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const restaurant = location.state?.restaurant;

    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadReviews = async () => {
            try {
                setLoading(true);
                setError("");

                const data = await getReviewsByPlace(id);

                setReviews(data);
            } catch (err) {
                setError(
                    err?.message ||
                    "Unable to load reviews."
                );
            } finally {
                setLoading(false);
            }
        };

        loadReviews();
    }, [id]);

    const restaurantName =
        restaurant?.name ||
        restaurant?.display_name ||
        "Restaurant";

    const formatDate = (dateValue) => {
        if (!dateValue) {
            return "";
        }

        const date = new Date(dateValue);

        if (Number.isNaN(date.getTime())) {
            return "";
        }

        return date.toLocaleDateString("en-IN", {
            day: "numeric",
            month: "short",
            year: "numeric"
        });
    };

    return (
        <div className="restaurant-reviews-page">

            <div className="reviews-page-container">

                <button
                    type="button"
                    className="back-button"
                    onClick={() => navigate(-1)}
                >
                    ← Back
                </button>

                <div className="reviews-page-header">
                    <div>
                        <span className="restaurant-details-category">
                            🍽️ Restaurant
                        </span>

                        <h1>{restaurantName}</h1>

                        <p className="reviews-page-subtitle">
                            Customer Reviews
                        </p>
                    </div>

                    <button
                        type="button"
                        className="write-review-button"
                        onClick={() =>
                            navigate(`/restaurant/${id}`, {
                                state: {
                                    restaurant,
                                    openReview: true
                                }
                            })
                        }
                    >
                        ⭐ Write a Review
                    </button>
                </div>

                {loading && (
                    <div className="reviews-loading">
                        Loading reviews...
                    </div>
                )}

                {!loading && error && (
                    <div className="reviews-error">
                        {error}
                    </div>
                )}

                {!loading && !error && reviews.length === 0 && (
                    <div className="no-reviews">
                        <div className="no-reviews-icon">
                            💬
                        </div>

                        <h2>No reviews yet</h2>

                        <p>
                            Be the first person to review this restaurant.
                        </p>

                        <button
                            type="button"
                            className="write-review-button"
                            onClick={() =>
                                navigate(`/restaurant/${id}`, {
                                    state: {
                                        restaurant,
                                        openReview: true
                                    }
                                })
                            }
                        >
                            ⭐ Write the First Review
                        </button>
                    </div>
                )}

                {!loading && !error && reviews.length > 0 && (
                    <div className="reviews-list">

                        <div className="reviews-count">
                            {reviews.length}{" "}
                            {reviews.length === 1
                                ? "Review"
                                : "Reviews"}
                        </div>

                        {reviews.map((review) => (
                            <div
                                className="review-card"
                                key={review.id}
                            >

                                <div className="review-card-header">

                                    <div className="reviewer-info">

                                        <div className="reviewer-avatar">
                                            {(review.reviewerEmail ||
                                                "U")
                                                .charAt(0)
                                                .toUpperCase()}
                                        </div>

                                        <div>
                                            <div className="reviewer-name">
                                                {review.reviewerEmail ||
                                                    "Anonymous User"}
                                            </div>

                                            <div className="review-date">
                                                {formatDate(
                                                    review.createdAt
                                                )}
                                            </div>
                                        </div>

                                    </div>

                                    <div className="review-rating">
                                        {"★".repeat(
                                            Number(review.rating) || 0
                                        )}
                                        <span>
                                            {"★".repeat(
                                                5 -
                                                (Number(review.rating) ||
                                                    0)
                                            )}
                                        </span>
                                    </div>

                                </div>

                                <div className="review-content">

                                    {review.title && (
                                        <h3>
                                            {review.title}
                                        </h3>
                                    )}

                                    <p>
                                        {review.content}
                                    </p>

                                </div>

                            </div>
                        ))}

                    </div>
                )}

            </div>
        </div>
    );
};

export default RestaurantReviews;