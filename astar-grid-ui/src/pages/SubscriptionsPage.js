import React, { useEffect, useState } from "react";

const SubscriptionsPage = () => {
    const [subscriptions, setSubscriptions] = useState([]);
    const [projectDetails, setProjectDetails] = useState({}); // Храним информацию о проектах

    useEffect(() => {
        fetch("http://localhost:8082/subscribes/subscribes_list")
            .then((res) => res.json())
            .then((data) => {
                setSubscriptions(data);
                data.forEach((sub) => fetchProjectDetails(sub.projectId)); // Загружаем информацию о проектах
            })
            .catch((error) => console.error("Ошибка загрузки подписок:", error));
    }, []);

    const fetchProjectDetails = (projectId) => {
        fetch(`http://localhost:8082/project/${projectId}`)
            .then((res) => res.json())
            .then((project) => {
                setProjectDetails((prevDetails) => ({
                    ...prevDetails,
                    [projectId]: project[0] // Берем первый элемент списка (сервер возвращает массив)
                }));
            })
            .catch((error) => console.error("Ошибка загрузки проекта:", error));
    };

    const unsubscribe = (projectId) => {
        fetch(`http://localhost:8082/subscribes/unsubscribe?id=${projectId}`, { method: "POST" })
            .then(() => {
                setSubscriptions(subscriptions.filter((sub) => sub.projectId !== projectId));
                setProjectDetails((prevDetails) => {
                    const newDetails = { ...prevDetails };
                    delete newDetails[projectId];
                    return newDetails;
                });
            })
            .catch((error) => console.error("Ошибка отписки:", error));
    };

    return (
        <div style={styles.container}>
            <h2>Мои подписки</h2>
            {subscriptions.length === 0 ? (
                <p>У вас нет активных подписок.</p>
            ) : (
                <div style={styles.gridContainer}>
                    {subscriptions.map((sub) => {
                        const project = projectDetails[sub.projectId];

                        return (
                            <div key={sub.projectId} style={styles.subscriptionCard}>
                                <h3>{project ? project.name : `Проект ${sub.projectId}`}</h3>
                                {project && (
                                    <>
                                        <p style={styles.reward}>Награда: {project.reward} ₽</p>
                                        <p style={styles.description}>{project.description}</p>
                                    </>
                                )}

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
                        );
                    })}
                </div>
            )}
        </div>
    );
};

// Функция форматирования времени из секунд в формат hh:mm
const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
};

// Стили
const styles = {
    container: {
        maxWidth: "900px",
        margin: "0 auto",
        padding: "20px",
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
        borderRadius: "8px",
        backgroundColor: "#f9f9f9",
        textAlign: "center",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)"
    },
    reward: {
        fontSize: "18px",
        fontWeight: "bold",
        color: "#2d7d46",
        marginBottom: "10px"
    },
    description: {
        fontSize: "14px",
        fontStyle: "italic",
        color: "#555",
        marginBottom: "10px"
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
