import React, { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import {
    createReview,
    hasMyReview
} from "../../services/reviewService";

const ReviewForm = ({ placeId, onReviewCreated }) => {
    const { user } = useAuth();

    const [rating, setRating] = useState(0);
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");

    const [submitting, setSubmitting] = useState(false);
    const [checkingReview, setCheckingReview] = useState(true);
    const [alreadyReviewed, setAlreadyReviewed] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        const checkReview = async () => {
            if (!user || !placeId) {
                setCheckingReview(false);
                return;
            }

            try {
                const reviewed = await hasMyReview(placeId);
                setAlreadyReviewed(reviewed);
            } catch {
                setAlreadyReviewed(false);
            } finally {
                setCheckingReview(false);
            }
        };

        checkReview();
    }, [user, placeId]);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!user) {
            setError("Please login to write a review.");
            return;
        }

        if (alreadyReviewed) {
            setError(
                "You have already reviewed this restaurant."
            );
            return;
        }

        if (!rating) {
            setError("Please select a rating.");
            return;
        }

        if (!title.trim()) {
            setError("Please enter a review title.");
            return;
        }

        if (content.trim().length < 5) {
            setError(
                "Review content must be at least 5 characters."
            );
            return;
        }

        try {
            setSubmitting(true);

            await createReview({
                placeId,
                rating,
                title: title.trim(),
                content: content.trim()
            });

            setRating(0);
            setTitle("");
            setContent("");

            setAlreadyReviewed(true);

            setSuccess(
                "Your review was submitted successfully."
            );

            if (onReviewCreated) {
                onReviewCreated();
            }

        } catch (err) {
            setError(
                err?.message ||
                "Unable to submit review."
            );
        } finally {
            setSubmitting(false);
        }
    };

    if (checkingReview) {
        return (
            <div className="review-form">
                Checking review status...
            </div>
        );
    }

    if (!user) {
        return (
            <div className="review-form">
                <h2>Write a Review</h2>

                <p>
                    Please login to write a review.
                </p>
            </div>
        );
    }

    if (alreadyReviewed) {
        return (
            <div className="review-form">

                <h2>Your Review</h2>

                <div className="review-already-submitted">
                    ✓ You have already reviewed this
                    restaurant.
                </div>

                <p>
                    You can view your review and other
                    customer reviews from the Reviews page.
                </p>

            </div>
        );
    }

    return (
        <form
            className="review-form"
            onSubmit={handleSubmit}
        >

            <h2>Write a Review</h2>

            {error && (
                <div className="review-error">
                    {error}
                </div>
            )}

            {success && (
                <div className="review-success">
                    {success}
                </div>
            )}

            <div className="review-field">

                <label>
                    Rating
                </label>

                <div className="rating-selector">

                    {[1, 2, 3, 4, 5].map((value) => (
                        <button
                            key={value}
                            type="button"
                            className={
                                value <= rating
                                    ? "rating-star selected"
                                    : "rating-star"
                            }
                            onClick={() =>
                                setRating(value)
                            }
                        >
                            ★
                        </button>
                    ))}

                </div>

            </div>

            <div className="review-field">

                <label htmlFor="review-title">
                    Title
                </label>

                <input
                    id="review-title"
                    type="text"
                    value={title}
                    onChange={(event) =>
                        setTitle(event.target.value)
                    }
                    placeholder="Give your review a title"
                    maxLength={150}
                />

            </div>

            <div className="review-field">

                <label htmlFor="review-content">
                    Review
                </label>

                <textarea
                    id="review-content"
                    value={content}
                    onChange={(event) =>
                        setContent(event.target.value)
                    }
                    placeholder="Tell us about your experience"
                    rows={6}
                    maxLength={5000}
                />

            </div>

            <button
                type="submit"
                className="submit-review-button"
                disabled={submitting}
            >
                {submitting
                    ? "Submitting..."
                    : "Submit Review"}
            </button>

        </form>
    );
};

export default ReviewForm;