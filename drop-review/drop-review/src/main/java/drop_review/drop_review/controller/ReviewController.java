package drop_review.drop_review.controller;

import drop_review.drop_review.dto.ReviewRequest;
import drop_review.drop_review.entity.Review;
import drop_review.drop_review.service.ReviewService;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(
        origins = "http://localhost:5173"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    // =====================================================
    // CREATE REVIEW
    // =====================================================

    @PostMapping
    public ResponseEntity<Review> createReview(
            @Valid
            @RequestBody
            ReviewRequest request,

            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        String email =
                getEmail(authentication);

        Review review =
                reviewService.createReview(
                        request,
                        userId,
                        email
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(review);
    }

    // =====================================================
    // GET REVIEW BY ID
    // =====================================================

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReview(
            @PathVariable Long reviewId
    ) {

        Review review =
                reviewService.getReviewById(
                        reviewId
                );

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.maxAge(
                                30,
                                TimeUnit.SECONDS
                        ).cachePublic()
                )
                .body(review);
    }

    // =====================================================
    // GET ALL REVIEWS FOR RESTAURANT
    // =====================================================

    @GetMapping("/place/{placeId}")
    public ResponseEntity<List<Review>> getReviewsByPlace(
            @PathVariable String placeId
    ) {

        List<Review> reviews =
                reviewService.getReviewsByPlace(
                        placeId
                );

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.maxAge(
                                15,
                                TimeUnit.SECONDS
                        ).cachePublic()
                )
                .body(reviews);
    }

    // =====================================================
    // CHECK CURRENT USER REVIEW
    // =====================================================

    @GetMapping("/place/{placeId}/mine")
    public ResponseEntity<Boolean> hasMyReview(
            @PathVariable String placeId,
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        boolean reviewed =
                reviewService.hasUserReviewed(
                        userId,
                        placeId
                );

        // User-specific response.
        // Do NOT mark this as public cacheable.
        return ResponseEntity.ok()
                .body(reviewed);
    }

    // =====================================================
    // GET MY REVIEWS
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<List<Review>> getMyReviews(
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        List<Review> reviews =
                reviewService.getReviewsByUser(
                        userId
                );

        // User-specific response.
        // Browser/proxy must not publicly cache it.
        return ResponseEntity
                .ok()
                .body(reviews);
    }

    // =====================================================
    // DELETE REVIEW
    // =====================================================

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication
    ) {

        Long userId =
                getUserId(authentication);

        reviewService.deleteReview(
                reviewId,
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // =====================================================
    // GET USER ID
    // =====================================================

    private Long getUserId(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || authentication.getPrincipal() == null
        ) {
            throw new IllegalStateException(
                    "Authentication required"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }

        try {
            return Long.parseLong(
                    principal.toString()
            );
        } catch (NumberFormatException exception) {

            throw new IllegalStateException(
                    "Invalid authenticated user"
            );
        }
    }

    // =====================================================
    // GET EMAIL
    // =====================================================

    private String getEmail(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new IllegalStateException(
                    "Authentication required"
            );
        }

        Object details =
                authentication.getDetails();

        if (details instanceof String) {
            return (String) details;
        }

        return authentication.getName();
    }
}