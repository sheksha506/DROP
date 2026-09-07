package drop_review.drop_review.service;

import drop_review.drop_review.dto.ReviewRequest;
import drop_review.drop_review.entity.Review;
import drop_review.drop_review.repository.ReviewRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(
            ReviewRepository reviewRepository
    ) {
        this.reviewRepository = reviewRepository;
    }

    // =====================================================
    // CREATE REVIEW
    // =====================================================

    @Transactional
    public Review createReview(
            ReviewRequest request,
            Long userId,
            String email
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Review request cannot be null"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        String placeId =
                request.getPlaceId();

        if (
                placeId == null
                        || placeId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Restaurant ID is required"
            );
        }

        // =================================================
        // VALIDATE RATING
        // =================================================

        Integer rating =
                request.getRating();

        if (
                rating == null
                        || rating < 1
                        || rating > 5
        ) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }

        // =================================================
        // CHECK DUPLICATE
        // =================================================

        if (
                reviewRepository
                        .existsByUserIdAndPlaceId(
                                userId,
                                placeId
                        )
        ) {
            throw new IllegalStateException(
                    "You have already reviewed this restaurant."
            );
        }

        // =================================================
        // CREATE REVIEW
        // =================================================

        Review review =
                new Review();

        // User ID comes ONLY from JWT.
        review.setUserId(userId);

        // Email comes from authenticated user.
        review.setReviewerEmail(
                email == null
                        ? ""
                        : email
        );

        review.setPlaceId(
                placeId.trim()
        );

        review.setRating(
                rating
        );

        review.setTitle(
                safe(request.getTitle())
        );

        review.setContent(
                safe(request.getContent())
        );

        // =================================================
        // DATABASE SAFETY NET
        // =================================================

        try {

            return reviewRepository.save(
                    review
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            // The unique constraint
            // (user_id, place_id)
            // protects against concurrent duplicates.

            throw new IllegalStateException(
                    "You have already reviewed this restaurant."
            );
        }
    }

    // =====================================================
    // GET REVIEW BY ID
    // =====================================================

    public Review getReviewById(
            Long reviewId
    ) {

        if (reviewId == null) {
            throw new IllegalArgumentException(
                    "Review ID is required"
            );
        }

        return reviewRepository
                .findById(reviewId)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Review not found"
                                )
                );
    }

    // =====================================================
    // GET ALL REVIEWS FOR RESTAURANT
    // =====================================================

    public List<Review> getReviewsByPlace(
            String placeId
    ) {

        if (
                placeId == null
                        || placeId.isBlank()
        ) {
            return List.of();
        }

        return reviewRepository
                .findByPlaceIdOrderByCreatedAtDesc(
                        placeId.trim()
                );
    }

    // =====================================================
    // GET REVIEWS BY USER
    // =====================================================

    public List<Review> getReviewsByUser(
            Long userId
    ) {

        if (userId == null) {
            return List.of();
        }

        return reviewRepository
                .findByUserIdOrderByCreatedAtDesc(
                        userId
                );
    }

    // =====================================================
    // CHECK WHETHER USER REVIEWED RESTAURANT
    // =====================================================

    public boolean hasUserReviewed(
            Long userId,
            String placeId
    ) {

        if (
                userId == null
                        || placeId == null
                        || placeId.isBlank()
        ) {
            return false;
        }

        return reviewRepository
                .existsByUserIdAndPlaceId(
                        userId,
                        placeId.trim()
                );
    }

    // =====================================================
    // GET CURRENT USER'S REVIEW
    // =====================================================

    public Review getUserReview(
            Long userId,
            String placeId
    ) {

        if (
                userId == null
                        || placeId == null
                        || placeId.isBlank()
        ) {
            return null;
        }

        return reviewRepository
                .findByUserIdAndPlaceId(
                        userId,
                        placeId.trim()
                )
                .orElse(null);
    }

    // =====================================================
    // DELETE REVIEW
    // =====================================================

    @Transactional
    public void deleteReview(
            Long reviewId,
            Long userId
    ) {

        if (
                reviewId == null
                        || userId == null
        ) {
            throw new IllegalArgumentException(
                    "Review ID and user ID are required"
            );
        }

        Review review =
                getReviewById(
                        reviewId
                );

        // =================================================
        // OWNER CHECK
        // =================================================

        if (
                !userId.equals(
                        review.getUserId()
                )
        ) {

            throw new RuntimeException(
                    "You are not allowed to delete this review"
            );
        }

        reviewRepository.delete(
                review
        );
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}