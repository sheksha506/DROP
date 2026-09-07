package drop_review.drop_review.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(
                        name = "idx_review_place_created",
                        columnList = "place_id, created_at"
                ),
                @Index(
                        name = "idx_review_user_created",
                        columnList = "user_id, created_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_place",
                        columnNames = {
                                "user_id",
                                "place_id"
                        }
                )
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================================
    // USER
    // =====================================================

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    // =====================================================
    // RESTAURANT / PLACE
    // =====================================================

    @Column(
            name = "place_id",
            nullable = false,
            length = 100
    )
    private String placeId;

    // =====================================================
    // RATING
    // =====================================================

    @Column(
            nullable = false
    )
    private Integer rating;

    // =====================================================
    // TITLE
    // =====================================================

    @Column(
            nullable = false,
            length = 150
    )
    private String title;

    // =====================================================
    // CONTENT
    // =====================================================

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    // =====================================================
    // REVIEWER EMAIL
    // =====================================================

    @Column(
            name = "reviewer_email",
            nullable = false,
            length = 255
    )
    private String reviewerEmail;

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    @Column(
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            nullable = false
    )
    private LocalDateTime updatedAt;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Review() {
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }

    // =====================================================
    // AUTOMATIC TIMESTAMPS
    // =====================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}