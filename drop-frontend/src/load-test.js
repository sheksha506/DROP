import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "http://localhost:8081";

export const options = {
    stages: [
        { duration: "30s", target: 5 },
        { duration: "30s", target: 10 },
        { duration: "30s", target: 20 },
        { duration: "30s", target: 30 },
        { duration: "30s", target: 0 }
    ]
};

export default function () {

    const health = http.get(
        `${BASE_URL}/actuator/health`
    );

    check(health, {
        "health works": (response) =>
            response.status === 200
    });

    const random = http.get(
        `${BASE_URL}/api/places/random`
    );

    check(random, {
        "random restaurants works": (response) =>
            response.status === 200 ||
            response.status === 429
    });

    const nearby = http.get(
        `${BASE_URL}/api/places/nearby?latitude=17.3850&longitude=78.4867&radius=5000`
    );

    check(nearby, {
        "nearby restaurants works": (response) =>
            response.status === 200 ||
            response.status === 429
    });

    sleep(1);
}