import React, {
    createContext,
    useContext,
    useEffect,
    useState
} from "react";

import {
    login as loginApi,
    signup as signupApi,
    logout as logoutApi
} from "../services/authService";


// ==========================================
// CREATE CONTEXT
// ==========================================

const AuthContext = createContext(null);


// ==========================================
// AUTH PROVIDER
// ==========================================

export function AuthProvider({ children }) {

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);


    // ==========================================
    // LOAD USER WHEN APP STARTS
    // ==========================================

    useEffect(() => {

        const token =
            localStorage.getItem("token");

        const userId =
            localStorage.getItem("userId");

        const role =
            localStorage.getItem("role");

        const name =
            localStorage.getItem("name");

        const email =
            localStorage.getItem("email");


        if (token && userId) {

            setUser({
                token: token,
                userId: userId,
                role: role,
                name: name,
                email: email
            });
        }


        setLoading(false);

    }, []);


    // ==========================================
    // LOGIN
    // ==========================================

    const login = async (
        email,
        password
    ) => {

        const data =
            await loginApi(
                email,
                password
            );


        // ==========================================
        // GET USER NAME
        // ==========================================

        const loggedInName =
            data.name ||
            localStorage.getItem("name") ||
            "User";


        const loggedInEmail =
            data.email ||
            email;


        // ==========================================
        // SAVE TO LOCAL STORAGE
        // ==========================================

        localStorage.setItem(
            "token",
            data.token
        );

        localStorage.setItem(
            "userId",
            data.userId
        );

        localStorage.setItem(
            "role",
            data.role
        );

        localStorage.setItem(
            "name",
            loggedInName
        );

        localStorage.setItem(
            "email",
            loggedInEmail
        );


        // ==========================================
        // UPDATE USER
        // ==========================================

        const loggedInUser = {

            token: data.token,

            userId: data.userId,

            role: data.role,

            name: loggedInName,

            email: loggedInEmail
        };


        setUser(loggedInUser);


        return data;
    };


    // ==========================================
    // SIGNUP
    // ==========================================

    const signup = async (
        name,
        email,
        password
    ) => {

        const data =
            await signupApi(
                name,
                email,
                password
            );


        // ==========================================
        // SAVE TO LOCAL STORAGE
        // ==========================================

        localStorage.setItem(
            "token",
            data.token
        );

        localStorage.setItem(
            "userId",
            data.userId
        );

        localStorage.setItem(
            "role",
            data.role
        );

        localStorage.setItem(
            "name",
            name
        );

        localStorage.setItem(
            "email",
            email
        );


        // ==========================================
        // UPDATE USER
        // ==========================================

        const signedUpUser = {

            token: data.token,

            userId: data.userId,

            role: data.role,

            name: name,

            email: email
        };


        setUser(signedUpUser);


        return data;
    };


    // ==========================================
    // LOGOUT
    // ==========================================

    const logout = () => {

        // Call backend logout if your
        // auth service has one.
        logoutApi();


        // ==========================================
        // CLEAR AUTHENTICATION DATA
        // ==========================================

        localStorage.removeItem("token");

        localStorage.removeItem("userId");

        localStorage.removeItem("role");

        localStorage.removeItem("name");

        localStorage.removeItem("email");


        // ==========================================
        // CLEAR RESTAURANT DATA
        // ==========================================

        sessionStorage.removeItem(
            "dropRestaurants"
        );

        sessionStorage.removeItem(
            "dropRestaurantsLatitude"
        );

        sessionStorage.removeItem(
            "dropRestaurantsLongitude"
        );


        // ==========================================
        // CLEAR LOCATION DATA
        // ==========================================

        sessionStorage.removeItem(
            "dropLocationGranted"
        );

        sessionStorage.removeItem(
            "dropLatitude"
        );

        sessionStorage.removeItem(
            "dropLongitude"
        );


        // ==========================================
        // UPDATE AUTH STATE
        // ==========================================

        setUser(null);
    };


    // ==========================================
    // CONTEXT
    // ==========================================

    return (

        <AuthContext.Provider
            value={{
                user,
                loading,
                isAuthenticated: !!user,
                login,
                signup,
                logout
            }}
        >

            {children}

        </AuthContext.Provider>
    );
}


// ==========================================
// USE AUTH
// ==========================================

export function useAuth() {

    const context =
        useContext(AuthContext);


    if (!context) {

        throw new Error(
            "useAuth must be used inside AuthProvider"
        );
    }


    return context;
}