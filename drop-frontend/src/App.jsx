import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import RestaurantDetails from "./pages/RestaurantDetails";
import RestaurantReviews from "./pages/RestaurantReviews";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

function App() {
    return (
        <BrowserRouter>
            <Routes>

                <Route
                    path="/"
                    element={<Home />}
                />

                <Route
                    path="/restaurant/:id"
                    element={<RestaurantDetails />}
                />

                <Route
                    path="/restaurant/:id/reviews"
                    element={<RestaurantReviews />}
                />

                <Route
                    path="/login"
                    element={<Login />}
                />

                <Route
                    path="/signup"
                    element={<Signup />}
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;