package drop_review.drop_review.repository;

import drop_review.drop_review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    // =====================================================
    // GET REVIEWS FOR RESTAURANT
    // =====================================================

    List<Review> findByPlaceIdOrderByCreatedAtDesc(
            String placeId
    );

    // =====================================================
    // GET REVIEWS BY USER
    // =====================================================

    List<Review> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    // =====================================================
    // CHECK EXISTING REVIEW
    // =====================================================

    boolean existsByUserIdAndPlaceId(
            Long userId,
            String placeId
    );

    // =====================================================
    // GET USER REVIEW FOR RESTAURANT
    // =====================================================

    Optional<Review> findByUserIdAndPlaceId(
            Long userId,
            String placeId
    );
}