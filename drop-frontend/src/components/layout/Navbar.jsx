import React from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const Navbar = ({
    searchQuery = "",
    onSearch
}) => {

    const navigate = useNavigate();

    const {
        user,
        isAuthenticated,
        logout
    } = useAuth();

    const handleSearch = (event) => {
        const value = event.target.value;

        if (onSearch) {
            onSearch(value);
        }
    };

    const handleLogout = async () => {
        try {
            await logout();
        } catch (error) {
            console.error(
                "Logout error:",
                error
            );
        }

        navigate("/login");
    };

    const displayName =
        user?.name ||
        user?.email?.split("@")[0] ||
        "User";

    return (
        <header className="navbar">

            <div className="navbar-container">

                {/* =================================
                    LOGO
                ================================= */}

                <div
                    className="navbar-logo"
                    onClick={() => navigate("/")}
                >
                    drop
                </div>


                {/* =================================
                    LOCATION
                ================================= */}

                <div className="navbar-location">

                    <span className="navbar-location-icon">
                        📍
                    </span>

                    <div className="navbar-location-text">

                        <span>
                            Your location
                        </span>

                        <strong>
                            Bangalore
                        </strong>

                    </div>

                </div>


                {/* =================================
                    SEARCH
                ================================= */}

                <div className="navbar-search">

                    <span className="navbar-search-icon">
                        🔍
                    </span>

                    <input
                        type="text"
                        placeholder="Search restaurants..."
                        value={searchQuery}
                        onChange={handleSearch}
                    />

                </div>


                {/* =================================
                    RIGHT SIDE
                ================================= */}

                <div className="navbar-right">

                    {isAuthenticated ? (
                        <>

                            <div className="navbar-user">

                                <span className="navbar-user-icon">
                                    👤
                                </span>

                                <strong>
                                    {displayName}
                                </strong>

                            </div>

                            <button
                                type="button"
                                className="navbar-logout"
                                onClick={handleLogout}
                            >
                                Logout
                            </button>

                        </>
                    ) : (
                        <div className="navbar-auth-buttons">

                            <button
                                type="button"
                                className="navbar-login"
                                onClick={() =>
                                    navigate("/login")
                                }
                            >
                                Login
                            </button>

                            <button
                                type="button"
                                className="navbar-signup"
                                onClick={() =>
                                    navigate("/signup")
                                }
                            >
                                Sign up
                            </button>

                        </div>
                    )}

                </div>

            </div>

        </header>
    );
};

export default Navbar;