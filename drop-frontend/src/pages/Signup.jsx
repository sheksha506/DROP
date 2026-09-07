import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";


function Signup() {

    const navigate = useNavigate();

    const { signup } = useAuth();


    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);


    // ==========================================
    // SIGNUP
    // ==========================================

    const handleSignup = async (e) => {

        e.preventDefault();

        setError("");


        // Check password confirmation

        if (password !== confirmPassword) {

            setError("Passwords do not match");

            return;
        }


        setLoading(true);


        try {

            // Call AuthContext
            // AuthContext -> authService -> drop-auth

            await signup(
                name,
                email,
                password
            );


            // Signup successful
            // User is already logged in because
            // AuthContext stores the JWT.

            navigate("/");


        } catch (error) {

            setError(
                error.message ||
                "Registration failed"
            );

        } finally {

            setLoading(false);
        }
    };


    return (

        <div className="login-page">

            <div className="login-container">

                <h1>Join DROP</h1>

                <p className="login-subtitle">
                    Create your account
                </p>


                {/* =====================================
                    ERROR MESSAGE
                ====================================== */}

                {error && (

                    <div className="login-error">
                        {error}
                    </div>

                )}


                {/* =====================================
                    SIGNUP FORM
                ====================================== */}

                <form onSubmit={handleSignup}>


                    {/* NAME */}

                    <div className="form-group">

                        <label>
                            Name
                        </label>

                        <input
                            type="text"
                            placeholder="Enter your name"
                            value={name}
                            onChange={(e) =>
                                setName(e.target.value)
                            }
                            required
                        />

                    </div>


                    {/* EMAIL */}

                    <div className="form-group">

                        <label>
                            Email
                        </label>

                        <input
                            type="email"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            required
                        />

                    </div>


                    {/* PASSWORD */}

                    <div className="form-group">

                        <label>
                            Password
                        </label>

                        <input
                            type="password"
                            placeholder="Create a password"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />

                    </div>


                    {/* CONFIRM PASSWORD */}

                    <div className="form-group">

                        <label>
                            Confirm Password
                        </label>

                        <input
                            type="password"
                            placeholder="Confirm your password"
                            value={confirmPassword}
                            onChange={(e) =>
                                setConfirmPassword(e.target.value)
                            }
                            required
                        />

                    </div>


                    {/* SIGNUP BUTTON */}

                    <button
                        type="submit"
                        disabled={loading}
                    >

                        {loading
                            ? "Creating account..."
                            : "Create account"
                        }

                    </button>

                </form>


                {/* =====================================
                    LOGIN LINK
                ====================================== */}

                <p className="signup-text">

                    Already have an account?

                    {" "}

                    <Link to="/login">
                        Login
                    </Link>

                </p>

            </div>

        </div>
    );
}


export default Signup;