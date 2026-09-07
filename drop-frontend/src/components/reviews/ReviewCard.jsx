import React from "react";


function ReviewCard({ review }) {

    return (

        <div className="review-card">

            {/* Rating */}

            <div className="review-card-rating">

                {Array.from(
                    { length: 5 },
                    (_, index) => (

                        <span
                            key={index}
                            className={
                                index < review.rating
                                    ? "review-star active"
                                    : "review-star"
                            }
                        >
                            ★
                        </span>

                    )
                )}

            </div>


            {/* Title */}

            <h3 className="review-card-title">

                {review.title}

            </h3>


            {/* Content */}

            <p className="review-card-content">

                {review.content}

            </p>


            {/* User */}

            <p className="review-card-user">

                User #{review.userId}

            </p>

        </div>
    );
}


export default ReviewCard;