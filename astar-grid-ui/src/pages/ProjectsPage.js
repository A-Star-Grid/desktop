import React, { useEffect, useState } from "react";

const ProjectsPage = () => {
    const [projects, setProjects] = useState([]);
    const [selectedProject, setSelectedProject] = useState(null);
    const [modalOpen, setModalOpen] = useState(false);

    // Параметры подписки
    const [cpu, setCpu] = useState(1);
    const [ram, setRam] = useState(512);
    const [disk, setDisk] = useState(10);
    const [startDay, setStartDay] = useState("Monday");
    const [endDay, setEndDay] = useState("Monday");
    const [startTime, setStartTime] = useState("00:00");
    const [endTime, setEndTime] = useState("23:59");

    useEffect(() => {
        fetch("http://localhost:8082/project/list?page=1&perPage=5")
            .then((res) => res.json())
            .then((data) => setProjects(data.projects))
            .catch((error) => console.error("Ошибка загрузки проектов:", error));
    }, []);

    const openModal = (project) => {
        setSelectedProject(project);
        setModalOpen(true);
    };

    const closeModal = () => {
        setModalOpen(false);
        setSelectedProject(null);
    };

    const subscribe = () => {
        if (!selectedProject) return;

        const requestBody = {
            projectId: selectedProject.id,
            scheduleIntervals: [
                {
                    start: { day: startDay, time: convertTimeToSeconds(startTime) },
                    end: { day: endDay, time: convertTimeToSeconds(endTime) },
                    computeResource: { cpuCores: cpu, diskSpace: disk, ram: ram }
                }
            ]
        };

        fetch("http://localhost:8082/subscribes/subscribe", {
            method: "POST",
            body: JSON.stringify(requestBody),
            headers: { "Content-Type": "application/json" }
        })
        .then(() => {
            alert(`Вы подписались на проект ${selectedProject.name}!`);
            closeModal();
        })
        .catch(error => console.error("Ошибка подписки:", error));
    };

    return (
        <div style={styles.container}>
            <h2>📌 Список проектов</h2>
            <div style={styles.gridContainer}>
                {projects.map((project) => (
                    <div key={project.id} style={styles.projectCard}>
                        <h3>{project.name}</h3>
                        <p>{project.description}</p>
                        <button onClick={() => openModal(project)} style={styles.subscribeButton}>Подписаться</button>
                    </div>
                ))}
            </div>

            {/* Модальное окно */}
            {modalOpen && (
                <div style={styles.modalOverlay}>
                    <div style={styles.modalContent}>
                        <h3>Настройки подписки на {selectedProject.name}</h3>

                        <label>CPU (ядра):</label>
                        <input type="number" value={cpu} onChange={(e) => setCpu(e.target.value)} />

                        <label>RAM (МБ):</label>
                        <input type="number" value={ram} onChange={(e) => setRam(e.target.value)} />

                        <label>Диск (ГБ):</label>
                        <input type="number" value={disk} onChange={(e) => setDisk(e.target.value)} />

                        <label>День начала:</label>
                        <select value={startDay} onChange={(e) => setStartDay(e.target.value)}>
                            {daysOfWeek.map(day => <option key={day} value={day}>{day}</option>)}
                        </select>

                        <label>Время начала:</label>
                        <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} />

                        <label>День окончания:</label>
                        <select value={endDay} onChange={(e) => setEndDay(e.target.value)}>
                            {daysOfWeek.map(day => <option key={day} value={day}>{day}</option>)}
                        </select>

                        <label>Время окончания:</label>
                        <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} />

                        <button onClick={subscribe} style={styles.subscribeButton}>Подтвердить подписку</button>
                        <button onClick={closeModal} style={styles.cancelButton}>Отмена</button>
                    </div>
                </div>
            )}
        </div>
    );
};

// Дни недели
const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

// Функция для конвертации времени в секунды (часы:минуты -> секунды)
const convertTimeToSeconds = (time) => {
    const [hours, minutes] = time.split(":").map(Number);
    return hours * 3600 + minutes * 60;
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
    projectCard: {
        border: "1px solid #ccc",
        padding: "15px",
        borderRadius: "5px",
        backgroundColor: "#f9f9f9",
        textAlign: "center",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)"
    },
    subscribeButton: {
        backgroundColor: "green",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px",
        marginTop: "10px"
    },
    modalOverlay: {
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        backgroundColor: "rgba(0,0,0,0.5)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center"
    },
    modalContent: {
        backgroundColor: "white",
        padding: "20px",
        borderRadius: "10px",
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        width: "300px"
    },
    cancelButton: {
        backgroundColor: "red",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px"
    }
};

export default ProjectsPage;
