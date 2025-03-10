import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const LoginPage = ({ setUsername }) => {
    const [username, setLocalUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleLogin = async () => {
        const response = await fetch(`http://localhost:8082/auth/login?username=${username}&password=${password}`, {
            method: "POST"
        });

        if (response.ok) {
            setUsername(username);
            navigate("/projects");
        } else {
            alert("Ошибка входа!");
        }
    };

    return (
        <div style={styles.container}>
            <h1>AStar Grid</h1>
            <input type="text" placeholder="Логин" value={username} onChange={(e) => setLocalUsername(e.target.value)} />
            <input type="password" placeholder="Пароль" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button onClick={handleLogin}>Войти</button>
            <button onClick={() => window.location.href = "https://example.com/register"}>Регистрация</button>
        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100vh",
        background: "url('/background.jpg') center/cover",
        color: "#fff",
    }
};

export default LoginPage;
