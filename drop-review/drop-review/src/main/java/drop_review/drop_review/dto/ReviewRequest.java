package drop_review.drop_review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    // =====================================================
    // RESTAURANT / PLACE
    // =====================================================

    @NotBlank(message = "Place ID is required")
    @Size(
            max = 100,
            message = "Place ID must not exceed 100 characters"
    )
    private String placeId;

    // =====================================================
    // RATING
    // =====================================================

    @NotNull(message = "Rating is required")
    @Min(
            value = 1,
            message = "Rating must be at least 1"
    )
    @Max(
            value = 5,
            message = "Rating must not exceed 5"
    )
    private Integer rating;

    // =====================================================
    // TITLE
    // =====================================================

    @NotBlank(message = "Title is required")
    @Size(
            min = 2,
            max = 150,
            message = "Title must be between 2 and 150 characters"
    )
    private String title;

    // =====================================================
    // CONTENT
    // =====================================================

    @NotBlank(message = "Review content is required")
    @Size(
            min = 5,
            max = 5000,
            message = "Review must be between 5 and 5000 characters"
    )
    private String content;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ReviewRequest() {
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(
            String placeId
    ) {
        this.placeId = placeId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(
            Integer rating
    ) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(
            String content
    ) {
        this.content = content;
    }
}