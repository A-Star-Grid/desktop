import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const Header = () => {
    const [username, setUsername] = useState(localStorage.getItem("username") || null);
    const navigate = useNavigate();

    useEffect(() => {
        if (!username) {
            fetch("http://localhost:8082/auth/getCurrentUser")
                .then(response => {
                    if (!response.ok) throw new Error("Ошибка запроса");
                    return response.json();
                })
                .then(data => {
                    setUsername(data.username);
                    localStorage.setItem("username", data.username); // Сохраняем в localStorage
                })
                .catch(() => {
                    setUsername(null);
                    localStorage.removeItem("username"); // Удаляем, если не авторизован
                });
        }
    }, [username]);

    const handleLogout = () => {
        fetch("http://localhost:8082/auth/logout", { method: "POST" })
            .then(() => {
                localStorage.removeItem("username"); // Удаляем username
                setUsername(null);
                navigate("/");
            })
            .catch(error => console.error("Ошибка выхода:", error));
    };

    if (username === null) {
        return null; // Пока загружаем данные, ничего не рендерим
    }

    return (
        <header style={styles.header}>
        <span style={styles.logo}>A*Grid</span>
            <div style={{ display: 'flex', alignItems: 'center' }}>
                
                <nav style={styles.nav}>
                    <Link to="/projects" style={styles.navLink}>Проекты</Link>
                    <span style={styles.separator}>|</span>
                    <Link to="/subscriptions" style={styles.navLink}>Подписки</Link>
                    <span style={styles.separator}>|</span>
                    <Link to="/settings" style={styles.navLink}>Настройки</Link>
                    <span style={styles.separator}>|</span>
                    <Link to="/statistics" style={styles.navLink}>Статистика</Link>
                    <span style={styles.separator}>|</span>
                    <span style={styles.username}>👤 {username || "Гость"}</span>
                    <button onClick={handleLogout} style={styles.logoutButton}>Выйти</button>
                </nav>
            </div>
        </header>
    );
};

const styles = {
    logo: {
        fontSize: '24px',
        fontWeight: 'bold',
    },
    header: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "15px 20px",
        background: "linear-gradient(135deg, #ff6600, #000000)", // Градиентный фон
        color: "#fff",
        boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.3)",
    },
    username: {
        fontWeight: "bold",
        fontSize: "18px",
    },
    nav: {
        display: "flex",
        alignItems: "center",
        gap: "10px",
    },
    navLink: {
        color: "#fff",
        textDecoration: "none",
        padding: "10px 15px",
        borderRadius: "5px",
        transition: "background 0.3s",
    },
    separator: {
        color: "#ccc",
        fontSize: "18px",
        margin: "0 5px",
    },
    logoutButton: {
        padding: "10px 15px",
        cursor: "pointer",
        border: "none",
        backgroundColor: "#ff4d4d",
        color: "white",
        borderRadius: "5px",
        transition: "background 0.3s",
    }
};

export default Header;
