import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const LandingPage = ({ setUsername }) => {
    const [showLogin, setShowLogin] = useState(false);
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
            {!showLogin ? (
                <>
                    <h1 style={styles.title}>A*GRID</h1>
                    <button style={styles.button} onClick={() => setShowLogin(true)}>Войти</button>
                    <button style={styles.button} onClick={() => window.location.href = "https://example.com/register"}>
                        Регистрация
                    </button>
                </>
            ) : (
                <div style={styles.loginBox}>
                    <h2>Вход</h2>
                    <input
                        type="text"
                        placeholder="Логин"
                        value={username}
                        onChange={(e) => setLocalUsername(e.target.value)}
                        style={styles.input}
                    />
                    <input
                        type="password"
                        placeholder="Пароль"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={styles.input}
                    />
                    <button style={styles.button} onClick={handleLogin}>Войти</button>
                    <button style={styles.cancelButton} onClick={() => setShowLogin(false)}>Назад</button>
                    
                    {/* 🟢 Ссылка для восстановления пароля */}
                    <a href="https://example.ru/reset-password" style={styles.resetLink}>Сбросить пароль</a>
                </div>
            )}
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
        background: "linear-gradient(135deg, #ff6600, #000000)",
        color: "#fff",
    },
    title: {
        fontSize: "50px",
        fontWeight: "bold",
    },
    button: {
        backgroundColor: "#ff6600",
        border: "none",
        padding: "15px 30px",
        margin: "10px",
        color: "#fff",
        fontSize: "18px",
        borderRadius: "10px",
        cursor: "pointer",
    },
    loginBox: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: "20px",
        background: "rgba(0, 0, 0, 0.7)",
        borderRadius: "10px",
    },
    input: {
        padding: "10px",
        margin: "10px 0",
        width: "250px",
        fontSize: "16px",
    },
    cancelButton: {
        backgroundColor: "#555",
        border: "none",
        padding: "10px 20px",
        marginTop: "10px",
        color: "#fff",
        fontSize: "16px",
        borderRadius: "5px",
        cursor: "pointer",
    },
    resetLink: {
        marginTop: "10px",
        fontSize: "14px",
        color: "rgba(197, 196, 193, 0.7)",
        cursor: "pointer",
        textDecoration: "none",
        display: "block",
        textAlign: "center"
    }
};

export default LandingPage;
