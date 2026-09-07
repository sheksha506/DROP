const REVIEW_BASE_URL = "http://localhost:8083/api/reviews";

export const createReview = async ({
    placeId,
    rating,
    title,
    content
}) => {
    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Please login to write a review.");
    }

    const response = await fetch(REVIEW_BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            placeId: String(placeId),
            rating: Number(rating),
            title,
            content
        })
    });

    const text = await response.text();

    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = null;
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `Review request failed (${response.status})`
        );
    }

    return data;
};


export const getReviewsByPlace = async (placeId) => {
    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Please login to view reviews.");
    }

    const response = await fetch(
        `${REVIEW_BASE_URL}/place/${encodeURIComponent(placeId)}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        }
    );

    const text = await response.text();

    let data = [];

    try {
        data = text ? JSON.parse(text) : [];
    } catch {
        data = [];
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `Unable to load reviews (${response.status})`
        );
    }

    return Array.isArray(data) ? data : [];
};


export const hasMyReview = async (placeId) => {
    const token = localStorage.getItem("token");

    if (!token) {
        return false;
    }

    const response = await fetch(
        `${REVIEW_BASE_URL}/place/${encodeURIComponent(placeId)}/mine`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        }
    );

    const text = await response.text();

    let data = false;

    try {
        data = text ? JSON.parse(text) : false;
    } catch {
        data = false;
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `Unable to check review status (${response.status})`
        );
    }

    return Boolean(data);
};