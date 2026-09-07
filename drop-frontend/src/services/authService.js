const AUTH_BASE_URL = "http://localhost:8082/auth";


// ==========================================
// LOGIN
// ==========================================

export const login = async (email, password) => {

    const response = await fetch(
        `${AUTH_BASE_URL}/login`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email,
                password
            })
        }
    );


    const data = await response.json();


    if (!response.ok) {

        throw new Error(
            data.message || "Login failed"
        );
    }


    return data;
};


// ==========================================
// SIGNUP
// ==========================================

export const signup = async (
    name,
    email,
    password
) => {

    const response = await fetch(
        `${AUTH_BASE_URL}/register`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                name,
                email,
                password
            })
        }
    );


    const data = await response.json();


    if (!response.ok) {

        throw new Error(
            data.message || "Registration failed"
        );
    }


    return data;
};


// ==========================================
// GET CURRENT USER
// ==========================================

export const getCurrentUser = async (token) => {

    const response = await fetch(
        `${AUTH_BASE_URL}/me`,
        {
            method: "GET",

            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        }
    );


    const data = await response.json();


    if (!response.ok) {

        throw new Error(
            data.message || "Unable to get current user"
        );
    }


    return data;
};


// ==========================================
// LOGOUT
// ==========================================

export const logout = () => {

    localStorage.removeItem("token");

    localStorage.removeItem("userId");

    localStorage.removeItem("role");

    localStorage.removeItem("name");

    localStorage.removeItem("email");
};


// ==========================================
// GET TOKEN
// ==========================================

export const getToken = () => {

    return localStorage.getItem("token");
};