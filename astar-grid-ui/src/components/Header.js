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
            <nav style={styles.nav}>
                <Link to="/projects" style={styles.navLink}>📌 Проекты</Link>
                <span style={styles.separator}>|</span>
                <Link to="/subscriptions" style={styles.navLink}>📜 Подписки</Link>
                <span style={styles.separator}>|</span>
                <Link to="/settings" style={styles.navLink}>⚙ Настройки</Link>
                <span style={styles.separator}>|</span>
                <Link to="/statistics" style={styles.navLink}>📊 Статистика</Link>
                <span style={styles.separator}>|</span>
                <button onClick={handleLogout} style={styles.logoutButton}>🚪 Выйти</button>
            </nav>
        </header>
    );
};

const styles = {
    header: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "10px 20px",
        backgroundColor: "#222",
        color: "#fff",
    },
    username: {
        fontWeight: "bold",
        fontSize: "16px",
    },
    nav: {
        display: "flex",
        alignItems: "center",
        gap: "10px",
    },
    navLink: {
        color: "#fff",
        textDecoration: "none",
        padding: "8px 15px",
        borderRadius: "5px",
        transition: "background 0.3s",
    },
    separator: {
        color: "#ccc",
        fontSize: "18px",
        margin: "0 5px",
    },
    logoutButton: {
        padding: "8px 15px",
        cursor: "pointer",
        border: "none",
        backgroundColor: "#ff4d4d",
        color: "white",
        borderRadius: "5px",
        transition: "background 0.3s",
    }
};

export default Header;
