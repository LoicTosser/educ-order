import {BrowserRouter as Router, Routes, Route, useHistory} from "react-router-dom";
import Login from "./components/login";
import Activities from "./components/dashboard";
import React, {Component, useEffect} from "react";

class App extends Component {

    render() {
        return (
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/" element={<Activities />} />
                </Routes>
            </Router>
        )
    }
}

export default App;
