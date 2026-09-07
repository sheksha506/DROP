# DROP 🍽️

### Discover. Explore. Review.

DROP is a full-stack restaurant discovery and review platform that I built as a personal project.

The idea behind DROP is simple: when a user wants to find a restaurant nearby, they can open the application, allow their location, discover nearby restaurants, search for restaurants, view restaurant details, and share their own reviews.

I wanted to build something more realistic than a basic CRUD application, so while developing DROP I worked on authentication, REST APIs, geolocation, external APIs, PostgreSQL, Redis, geospatial search, database indexing, API rate limiting, request deduplication, connection pooling, Docker, and cloud deployment.

---

# 🌐 Live Demo

🚀 **Live Website:** Coming soon

📦 **GitHub Repository:**  
https://github.com/sheksha506/DROP

> I will add the live website URL here after deployment.

---

# 📖 About DROP

DROP is a restaurant discovery and review application.

The application has two main experiences.

When a user is not logged in, the application can show restaurants that are already available in the application's database.

When a user logs in, DROP can request the user's browser location and use that location to find nearby restaurants.

After discovering a restaurant, users can open its details and, after authentication, submit reviews.

The application is divided into separate backend services for restaurant data, authentication, and reviews.

---

# 🎯 Project Goals

The main goals I had while building DROP were:

- Build a complete full-stack application
- Learn how React communicates with Spring Boot
- Build REST APIs
- Implement authentication and authorization
- Work with PostgreSQL
- Learn Redis caching
- Implement location-based restaurant discovery
- Work with external APIs
- Improve API performance
- Learn database indexing
- Implement API rate limiting
- Understand connection pooling
- Use Docker for backend services
- Deploy a real application
- Build a project that I can use in my portfolio and job applications

---

# ✨ Features

## 📍 Restaurant Discovery

Users can discover restaurants based on their location.

The browser provides the user's latitude and longitude.

The frontend sends the coordinates to the backend, and the backend searches for restaurants within a specific radius.

```text
User
 |
 | Browser Location
 ▼
Latitude + Longitude
 |
 ▼
DROP Backend
 |
 ▼
Restaurant Search
 |
 ▼
Nearby Restaurants
 |
 ▼
Frontend
