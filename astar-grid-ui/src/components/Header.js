import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const Header = () => {
    const [username, setUsername] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetch("http://localhost:8082/auth/getCurrentUser")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Ошибка запроса");
                }
                return response.json();
            })
            .then(data => {
                setUsername(data.username);
            })
            .catch(() => {
                setUsername(null);
            });
    }, []);

    const handleLogout = () => {
        fetch("http://localhost:8082/auth/logout", { method: "POST" })
            .then(() => navigate("/"))
            .catch(error => console.error("Ошибка выхода:", error));
    };

    if (username === null) {
        return null; // Пока загружаем данные, ничего не рендерим
    }

    return (
        <header style={styles.header}>
            <span style={styles.username}>👤 {username || "Гость"}</span>
            <nav>
                <Link to="/projects">📌 Проекты</Link>
                <Link to="/subscriptions">📜 Подписки</Link>
                <Link to="/settings">⚙ Настройки</Link>
                <Link to="/statistics">📊 Статистика</Link>
            </nav>
            <button onClick={handleLogout} style={styles.logoutButton}>🚪 Выйти</button>
        </header>
    );
};

const styles = {
    header: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "10px",
        backgroundColor: "#222",
        color: "#fff"
    },
    username: {
        fontWeight: "bold",
    },
    logoutButton: {
        marginLeft: "20px",
        padding: "5px 10px",
        cursor: "pointer",
        border: "none",
        backgroundColor: "#ff4d4d",
        color: "white",
        borderRadius: "5px",
    }
};

export default Header;
