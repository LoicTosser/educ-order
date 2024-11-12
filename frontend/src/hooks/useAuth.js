import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const useAuth = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const response = await fetch('http://localhost:8080/auth/status');
                const isAuthenticated = await response.json();
                if (!isAuthenticated) {
                    navigate('/login');
                }
            } catch (error) {
                console.error('Error checking authentication status', error);
                navigate('/login');
            }
        };

        checkAuth();
    }, [navigate]);
};

export default useAuth;