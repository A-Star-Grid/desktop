import React, { useEffect, useState } from "react";

const SubscriptionsPage = () => {
    const [subscriptions, setSubscriptions] = useState([]);

    useEffect(() => {
        fetch("http://localhost:8082/subscribes/subscribes_list")
            .then((res) => res.json())
            .then((data) => setSubscriptions(data))
            .catch((error) => console.error("Ошибка загрузки подписок:", error));
    }, []);

    const unsubscribe = (projectId) => {
        fetch(`http://localhost:8082/subscribes/unsubscribe?id=${projectId}`, { method: "POST" })
            .then(() => setSubscriptions(subscriptions.filter(sub => sub.projectId !== projectId)))
            .catch(error => console.error("Ошибка отписки:", error));
    };

    return (
        <div style={styles.container}>
            <h2>Мои подписки</h2>
            {subscriptions.length === 0 ? (
                <p>У вас нет активных подписок.</p>
            ) : (
                <div style={styles.gridContainer}>
                    {subscriptions.map((sub) => (
                        <div key={sub.projectId} style={styles.subscriptionCard}>
                            <h3>Проект {sub.projectId}</h3>

                            <h4>🔄 Интервалы запуска:</h4>
                            {sub.scheduleIntervals.map((interval, index) => (
                                <div key={index} style={styles.intervalBlock}>
                                    <p><b>День начала:</b> {interval.start.day}, <b>Время:</b> {formatTime(interval.start.time)}</p>
                                    <p><b>День конца:</b> {interval.end.day}, <b>Время:</b> {formatTime(interval.end.time)}</p>

                                    <h4>⚙ Ресурсы:</h4>
                                    <p><b>CPU:</b> {interval.computeResource.cpuCores} ядер</p>
                                    <p><b>Диск:</b> {interval.computeResource.diskSpace} ГБ</p>
                                    <p><b>RAM:</b> {interval.computeResource.ram} МБ</p>
                                </div>
                            ))}

                            <button onClick={() => unsubscribe(sub.projectId)} style={styles.unsubscribeButton}>
                                ❌ Отписаться
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

// Функция форматирования времени из секунд в формат hh:mm:ss
const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
};

// Стили
const styles = {
    container: {
        maxWidth: "900px", // Устанавливаем ограничение по ширине
        margin: "0 auto", // Центрируем контент
        padding: "20px", // Добавляем отступы
    },
    gridContainer: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
        gap: "15px",
        marginTop: "20px"
    },
    subscriptionCard: {
        border: "1px solid #ccc",
        padding: "15px",
        borderRadius: "5px",
        backgroundColor: "#f9f9f9",
        textAlign: "center",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)"
    },
    intervalBlock: {
        padding: "10px",
        backgroundColor: "#eaeaea",
        borderRadius: "5px",
        marginBottom: "10px",
    },
    unsubscribeButton: {
        marginTop: "10px",
        backgroundColor: "red",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px",
    }
};

export default SubscriptionsPage;
