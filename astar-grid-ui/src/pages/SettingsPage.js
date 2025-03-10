import React, { useState, useEffect } from "react";

const SettingsPage = () => {
    const [cpu, setCpu] = useState("");
    const [ram, setRam] = useState("");
    const [disk, setDisk] = useState("");
    const [vboxPath, setVBoxPath] = useState("");
    const [computationActive, setComputationActive] = useState(false);
    const [statusMessage, setStatusMessage] = useState("");

    // Загружаем текущие настройки при загрузке страницы
    useEffect(() => {
        fetch("http://localhost:8082/settings/current")
            .then(response => response.json())
            .then(data => {
                setCpu(data.cpuLimit);
                setRam(data.ramLimit);
                setDisk(data.diskLimit);
                setVBoxPath(data.virtualBoxPath);
                setComputationActive(data.computationActive);
            })
            .catch(error => console.error("Ошибка загрузки настроек:", error));
    }, []);

    const saveSettings = () => {
        const encodedPath = encodeURIComponent(vboxPath.replace(/\\/g, "/")); // Исправление слэшей

        Promise.all([
            fetch(`http://localhost:8082/settings/cpu?cpuCount=${cpu}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/ram?ramMB=${ram}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/disk?diskGB=${disk}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/virtualbox?path=${encodedPath}`, { method: "POST" })
        ])
            .then(() => setStatusMessage("✅ Настройки сохранены"))
            .catch(() => setStatusMessage("❌ Ошибка сохранения настроек"));
    };

    const resetSettings = () => {
        fetch("http://localhost:8082/settings/reset", { method: "POST" })
            .then(() => {
                setStatusMessage("⚙️ Настройки сброшены");
                setCpu("");
                setRam("");
                setDisk("");
                setVBoxPath("");
                setComputationActive(false);
            })
            .catch(() => setStatusMessage("❌ Ошибка сброса настроек"));
    };

    const toggleComputation = () => {
        fetch(`http://localhost:8082/settings/computation?isActive=${!computationActive}`, { method: "POST" })
            .then(() => setComputationActive(!computationActive))
            .catch(() => setStatusMessage("❌ Ошибка изменения состояния вычислений"));
    };

    return (
        <div style={styles.pageContainer}>
            <h2>⚙ Настройки</h2>

            <div style={styles.settingsForm}>
                <label>CPU:</label>
                <input type="number" value={cpu} onChange={(e) => setCpu(e.target.value)} />

                <label>RAM:</label>
                <input type="number" value={ram} onChange={(e) => setRam(e.target.value)} />

                <label>Disk:</label>
                <input type="number" value={disk} onChange={(e) => setDisk(e.target.value)} />

                <label>VirtualBox Path:</label>
                <input type="text" value={vboxPath} onChange={(e) => setVBoxPath(e.target.value)} />

                <div style={styles.buttonContainer}>
                    <button onClick={saveSettings} style={styles.saveButton}>Сохранить</button>
                    <button onClick={resetSettings} style={styles.resetButton}>Сбросить настройки</button>
                </div>
            </div>

            <h3>Статус вычислений: {computationActive ? "🟢 Активны" : "🔴 Остановлены"}</h3>
            <button onClick={toggleComputation} style={styles.toggleButton}>
                {computationActive ? "Остановить вычисления" : "Запустить вычисления"}
            </button>

            {statusMessage && <p style={styles.statusMessage}>{statusMessage}</p>}
        </div>
    );
};

// Стили
const styles = {
    pageContainer: {
        maxWidth: "600px", // Ограниечение ширины
        margin: "0 auto", // Центрирование страницы
        padding: "20px", // Внутренние отступы
    },
    settingsForm: {
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        backgroundColor: "#f9f9f9",
        padding: "15px",
        borderRadius: "10px",
        border: "1px solid #ccc",
    },
    buttonContainer: {
        display: "flex",
        justifyContent: "space-between",
        marginTop: "10px"
    },
    saveButton: {
        backgroundColor: "green",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px"
    },
    resetButton: {
        backgroundColor: "red",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px"
    },
    toggleButton: {
        marginTop: "10px",
        backgroundColor: "blue",
        color: "white",
        border: "none",
        padding: "10px",
        cursor: "pointer",
        borderRadius: "5px",
    },
    statusMessage: {
        marginTop: "10px",
        fontWeight: "bold",
        color: "#333"
    }
};

export default SettingsPage;
