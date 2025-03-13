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
    
    const [avatarSize, setAvatarSize] = useState(80);

    // Пагинация
    const [currentPage, setCurrentPage] = useState(1);
    const [perPage, setPerPage] = useState(5); // Кол-во проектов на странице
    const [totalPages, setTotalPages] = useState(1);
    
    // Поиск
    const [searchQuery, setSearchQuery] = useState("");

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

    const fetchProjects = (page, limit, search = null) => {
        const url = search && search.trim()
            ? `http://localhost:8082/project/search?name=${encodeURIComponent(search)}`
            : `http://localhost:8082/project/list?page=${page}&perPage=${limit}`;
    
        fetch(url)
            .then(res => res.json())
            .then(data => {
                const projectsWithColors = (Array.isArray(data) ? data : data.projects).map(project => ({
                    ...project,
                    color: generateRandomColor()
                }));
                setProjects(projectsWithColors);
                if (!search) setTotalPages(data.totalPages); // Только если не поиск, обновляем страницы
            })
            .catch(error => console.error("Ошибка загрузки проектов:", error));
    };
    

    useEffect(() => {
        fetchProjects(currentPage, perPage);
    }, [currentPage, perPage]);

    const goToNextPage = () => {
        if (currentPage < totalPages) {
            setCurrentPage(prev => prev + 1);
        }
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

            {/* Поле поиска */}
            <div style={styles.searchContainer}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => {
                        setSearchQuery(e.target.value);
                        fetchProjects(1, perPage, e.target.value);
                    }}
                    placeholder="Искать проект..."
                    style={styles.searchInput}
                />
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
                        <p style={styles.rewardText}> Награда: {project.reward} ₽ / за задачу</p>
                        <button onClick={() => openModal(project)} style={styles.subscribeButton}>Подписаться</button>
                    </div>
                ))}
            </div>
            
            <br/>

            {/* Выбор количества проектов на странице */}
            <div style={styles.paginationControls}>
                <label style={styles.paginationLabel}>Показывать проектов:</label>
                <select 
                    value={perPage} 
                    onChange={(e) => setPerPage(Number(e.target.value))} 
                    style={styles.paginationSelect}
                >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                </select>
            </div>


            {/* Улучшенная пагинация */}
            <div style={styles.pagination}>
                <button 
                    onClick={goToPrevPage} 
                    disabled={currentPage === 1} 
                    style={currentPage === 1 ? { ...styles.pageButton, ...styles.pageButtonDisabled } : styles.pageButton}>
                    ⬅ Назад
                </button>

                <span style={{ color: "black", fontWeight: "bold" }}>
                    Страница {currentPage} из {totalPages}
                </span>

                <button 
                    onClick={goToNextPage} 
                    disabled={currentPage === totalPages} 
                    style={currentPage === totalPages ? { ...styles.pageButton, ...styles.pageButtonDisabled } : styles.pageButton}>
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
    searchContainer: {
        display: "flex",
        justifyContent: "center",
        marginBottom: "15px",
    },
    searchInput: {
        width: "100%",
        padding: "10px",
        marginBottom: "15px",
        border: "1px solid #ccc",
        borderRadius: "5px",
        fontSize: "16px",
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
    },
    pagination: {
        border: "1px solid #ccc",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        marginTop: "20px",
        gap: "10px",
        background:  "#e6e6e6", //"linear-gradient(135deg, #ff6600, #000000)",
        padding: "10px",
        borderRadius: "10px",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
    },
    pageButton: {
        padding: "10px 20px",
        cursor: "pointer",
        border: "none",
        borderRadius: "5px",
        fontSize: "14px",
        fontWeight: "bold",
        background: "#ff6600",  // Цвет кнопки
        color: "white",
        transition: "background 0.3s ease-in-out",
    },
    pageButtonDisabled: {
        background: "#888",
        cursor: "not-allowed",
    },    
    paginationControls: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        gap: "10px",
      //  background: "#e6e6e6",// "linear-gradient(135deg, #ffffff, #e6e6e6)", // Градиентный фон
        padding: "12px 20px",
        borderRadius: "10px",
        boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
        marginBottom: "20px",
        fontSize: "16px",
        fontWeight: "bold",
        border: "1px solid #ccc",
    },
    
    paginationLabel: {
        color: "#333",
    },
    
    paginationSelect: {
        padding: "8px 12px",
        fontSize: "16px",
        fontWeight: "bold",
        borderRadius: "5px",
        border: "1px solid #ccc",
        cursor: "pointer",
        backgroundColor: "#fff",
        transition: "all 0.3s ease",
    },
    
    paginationSelectHover: {
        backgroundColor: "#f0f0f0",
    }
};

export default ProjectsPage;
