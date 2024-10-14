import logo from './logo.svg';
import './App.css';
import { BrowserRouter as Router, Routes, Route} from "react-router-dom";
import Home from "./components/home";
import Activities from "./components/dashboard";
import React, {Component} from "react";

class App extends Component {
    render() {
        return (
            <Router>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/dashboard" element={<Activities />} />
                </Routes>
            </Router>
        )
    }
}

export default App;
