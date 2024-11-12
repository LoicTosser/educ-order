import React from "react";
import '../styles/Login.css';

const Login = () => {

    const googleLogin = () => {
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    }

    return (
        <div className="login-container">
            <div className="login-form">
                <h2>Welcome to Educ Order</h2>
                <button onClick={googleLogin}>Login with Google</button>
            </div>
        </div>
    );
};

export default Login;