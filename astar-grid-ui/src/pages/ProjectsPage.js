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
    
    const [avatarSize, setAvatarSize] = useState(80); // Размер аватарки по умолчанию

    // Пагинация
    const [currentPage, setCurrentPage] = useState(1);
    const [perPage, setPerPage] = useState(5); // Кол-во проектов на странице
    const [totalPages, setTotalPages] = useState(1);
    

    // Обновляем размер аватарки при изменении ширины экрана
    useEffect(() => {
        const updateAvatarSize = () => {
            const newSize = Math.max(50, Math.min(window.innerWidth * 0.1, 120));
            setAvatarSize(newSize);
        };

        updateAvatarSize();
        window.addEventListener("resize", updateAvatarSize);
        return () => window.removeEventListener("resize", updateAvatarSize);
    }, []);

    useEffect(() => {
        fetchProjects(currentPage, perPage);
    }, [currentPage, perPage]); // Запрос будет обновляться при изменении страницы и лимита

    const fetchProjects = (page, limit) => {
        fetch(`http://localhost:8082/project/list?page=${page}&perPage=${limit}`)
            .then(res => res.json())
            .then(data => {
                const projectsWithColors = data.projects.map(project => ({
                    ...project,
                    color: generateRandomColor()
                }));
                setProjects(projectsWithColors);
                setTotalPages(data.totalPages); // Кол-во страниц из API
            })
            .catch(error => console.error("Ошибка загрузки проектов:", error));
    };

    useEffect(() => {
        fetchProjects(currentPage, perPage);
    }, [currentPage, perPage]);

    const goToNextPage = () => {
      //  if (currentPage < totalPages) {
            setCurrentPage(prev => prev + 1);
      //  }
    };

    const goToPrevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(prev => prev - 1);
        }
    };

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
            <h2>Список проектов</h2>
            {/* Выбор количества элементов на странице */}
            <div style={styles.paginationControls}>
                <label>Показывать:</label>
                <select value={perPage} onChange={(e) => setPerPage(Number(e.target.value))}>
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                </select>
            </div>

            <div style={styles.gridContainer}>
                {projects.map((project) => (
                    <div key={project.id} style={styles.projectCard}>
                        <div style={{ 
                            ...styles.projectAvatar, 
                            backgroundColor: project.color, 
                            width: avatarSize, 
                            height: avatarSize
                        }}></div>
                        <h3>{project.name}</h3>
                        <p>{project.description}</p>
                        <button onClick={() => openModal(project)} style={styles.subscribeButton}>Подписаться</button>
                    </div>
                ))}
            </div>

            {/* Пагинация */}
            <br/>
            <div style={styles.pagination}>
                <button onClick={goToPrevPage} disabled={currentPage === 1} style={styles.pageButton}>
                    ⬅ Назад
                </button>
                <span> Страница {currentPage} </span>
                <button onClick={goToNextPage} disabled={currentPage === totalPages} style={styles.pageButton}>
                    Вперед ➡
                </button>
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

// 🟢 Функция генерации случайного цвета
const generateRandomColor = () => {
    return `rgb(${rand(50, 200)}, ${rand(50, 200)}, ${rand(50, 200)})`;
};

// Функция для генерации случайного числа в диапазоне
const rand = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

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
        borderRadius: "10px",
        backgroundColor: "#f9f9f9",
        textAlign: "center",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
    },
    projectAvatar: {
        borderRadius: "10px",
        marginBottom: "10px"
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
