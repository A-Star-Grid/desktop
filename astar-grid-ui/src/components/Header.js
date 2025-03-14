import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const Header = () => {
    const [username, setUsername] = useState(localStorage.getItem("username") || null);
    const [email, setEmail] = useState("");
    const [balance, setBalance] = useState(0);
    const [isComputingActive, setIsComputingActive] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {        
        fetch("http://localhost:8082/auth/getCurrentUser")
            .then(response => response.json())
            .then(data => {
                setUsername(data.username);
                setEmail(data.email);
                setBalance(data.balance);
                localStorage.setItem("username", data.username);
            })
            .catch(() => {
                setUsername(null);
                localStorage.removeItem("username");
            });

        fetch("http://localhost:8082/settings/current")
            .then(response => response.json())
            .then(data => setIsComputingActive(data.computationActive))
            .catch(error => console.error("Ошибка получения статуса вычислений:", error));
    }, [username]);

    const handleLogout = () => {
        fetch("http://localhost:8082/auth/logout", { method: "POST" })
            .then(() => {
                localStorage.removeItem("username");
                setUsername(null);
                setIsModalOpen(false);
                navigate("/");
            })
            .catch(error => console.error("Ошибка выхода:", error));
    };

    const toggleComputing = () => {
        const newStatus = !isComputingActive;
        fetch(`http://localhost:8082/settings/computation?isActive=${newStatus}`, {
            method: "POST"
        })
            .then(() => setIsComputingActive(newStatus))
            .catch(error => console.error("Ошибка изменения статуса вычислений:", error));
    };

    const toggleModal = () => {
        setIsModalOpen(!isModalOpen);
    };

    if (username === null) {
        return null;
    }

    return (
        <>
            <header style={styles.header}>
                <span style={styles.logo}>A*Grid</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: "10px" }}>
                    <nav style={styles.nav}>
                        <Link to="/projects" style={styles.navLink}>Проекты</Link>
                        <span style={styles.separator}>|</span>
                        <Link to="/subscriptions" style={styles.navLink}>Подписки</Link>
                        <span style={styles.separator}>|</span>
                        <Link to="/settings" style={styles.navLink}>Настройки</Link>
                        <span style={styles.separator}>|</span>
                        <Link to="/statistics" style={styles.navLink}>Статистика</Link>
                        <span style={styles.separator}>|</span>
                        <span onClick={toggleModal} style={styles.username}>👤 {username || "Гость"}</span>
                        <button onClick={toggleComputing}
                            style={{
                                ...styles.computingButton,
                                backgroundColor: isComputingActive ? "#28a745" : "#ff4d4d"
                            }}>
                            {isComputingActive ? "⚡ Включено" : "Выключено"}
                        </button>
                    </nav>
                </div>
            </header>

            {/* Модальное окно с информацией о пользователе */}
            {isModalOpen && (
                <div style={styles.modalOverlay} onClick={toggleModal}>
                    <div style={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                        <div style={styles.iconContainer}>
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width="60"
                                height="60"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="black"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            >
                                <circle cx="12" cy="7" r="5"></circle>
                                <path d="M5 21v-2a7 7 0 0 1 14 0v2"></path>
                            </svg>
                        </div>
                        <p style={styles.usernameText}>{username}</p>
                        <p style={styles.emailText}>{email}</p>
                        <p onClick={() => window.location.href = "https://example.com/edit_user"} style={styles.editButton}>Редактировать</p>

                        <p style={styles.balanceText}>Баланс: {balance} ₽</p>
                        <p onClick={() => window.location.href = "https://example.com/get_coins"} style={styles.withdrawButton}>Вывод средств</p>

                        <button onClick={handleLogout} style={styles.logoutButton}>Выйти</button>
                        <button onClick={toggleModal} style={styles.closeButton}>Закрыть</button>
                    </div>
                </div>
            )}
        </>
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
        background: "linear-gradient(135deg, #ff6600, #000000)",
        color: "#fff",
        boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.3)",
    },
    username: {
        fontWeight: "bold",
        fontSize: "18px",
        cursor: "pointer",
        userSelect: "none",
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
    computingButton: {
        padding: "10px 15px",
        cursor: "pointer",
        border: "none",
        color: "white",
        borderRadius: "5px",
        transition: "background 0.3s",
    },
    modalOverlay: {
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 1000,
    },
    modalContent: {
        background: "#fff",
        padding: "20px",
        borderRadius: "10px",
        boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.3)",
        textAlign: "center",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        minWidth: "320px",
        maxWidth: "400px",
    },
    iconContainer: {
        marginBottom: "10px",
    },
    usernameText: {
        fontSize: "20px",
        fontWeight: "bold",
        marginBottom: "5px",
    },
    emailText: {
        fontSize: "16px",
        color: "#555",
        marginBottom: "5px",
    },
    editButton: {
        fontSize: "14px",
        color: "#777",
        textDecoration: "underline",
        cursor: "pointer",
        marginBottom: "15px",
        transition: "color 0.2s",
    },
    balanceText: {
        fontSize: "16px",
        color: "#28a745",
        fontWeight: "bold",
        marginBottom: "1px",
    },
    withdrawButton: {
        fontSize: "14px",
        color: "#777",
        textDecoration: "underline",
        cursor: "pointer",
        marginBottom: "20px",
        transition: "color 0.2s",
    },
    logoutButton: {
        width: "100%",
        marginBottom: "10px",
        background: "linear-gradient(135deg, #ff4d4d, #cc0000)",
        color: "white",
        border: "none",
        padding: "10px 15px",
        borderRadius: "5px",
        transition: "background 0.3s",
        cursor: "pointer", // Добавлено
    },
    closeButton: {
        width: "100%",
        background: "#ccc",
        color: "black",
        border: "none",
        padding: "8px 15px",
        borderRadius: "5px",
        transition: "background 0.3s",
        cursor: "pointer", // Добавлено
    },
};

export default Header;
