import React, { useState, useEffect } from "react";

const SettingsPage = () => {
    const [cpu, setCpu] = useState("");
    const [ram, setRam] = useState("");
    const [disk, setDisk] = useState("");
    const [vboxPath, setVBoxPath] = useState("");
    const [statusMessage, setStatusMessage] = useState("");

    const [initialCpu, setInitialCpu] = useState("");
    const [initialRam, setInitialRam] = useState("");
    const [initialDisk, setInitialDisk] = useState("");
    const [initialVBoxPath, setInitialVBoxPath] = useState("");

    // Общая функция загрузки текущих значений
    const loadCurrentSettings = () => {
        fetch("http://localhost:8082/settings/current")
            .then(response => response.json())
            .then(data => {
                setInitialCpu(data.cpuLimit);
                setInitialRam(data.ramLimit);
                setInitialDisk(data.diskLimit);
                setInitialVBoxPath(data.virtualBoxPath);
            })
            .catch(error => console.error("Ошибка загрузки настроек:", error));
    };

    useEffect(() => {
        loadCurrentSettings();
    }, []);

    const saveSettings = () => {
        const finalCpu = cpu || initialCpu;
        const finalRam = ram || initialRam;
        const finalDisk = disk || initialDisk;
        const finalVBoxPath = vboxPath || initialVBoxPath;
        const encodedPath = encodeURIComponent(finalVBoxPath.replace(/\\/g, "/"));

        Promise.all([
            fetch(`http://localhost:8082/settings/cpu?cpuCount=${finalCpu}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/ram?ramMB=${finalRam}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/disk?diskGB=${finalDisk}`, { method: "POST" }),
            fetch(`http://localhost:8082/settings/virtualbox?path=${encodedPath}`, { method: "POST" })
        ])
            .then(() => {
                setStatusMessage("✅ Настройки сохранены");
                setCpu("");
                setRam("");
                setDisk("");
                setVBoxPath("");
                loadCurrentSettings(); // Обновим значения
            })
            .catch(() => setStatusMessage("❌ Ошибка сохранения настроек"));
    };

    const resetSettings = () => {
        fetch("http://localhost:8082/settings/reset", { method: "POST" })
            .then(() => {
                setStatusMessage("Настройки сброшены");
                setCpu("");
                setRam("");
                setDisk("");
                setVBoxPath("");
                loadCurrentSettings(); // Обновим значения
            })
            .catch(() => setStatusMessage("Ошибка сброса настроек"));
    };

    return (
        <div style={styles.pageContainer}>
            <h2 style={styles.header}>⚙ Настройки виртуальной машины</h2>

            <div style={styles.settingsForm}>
                <div style={styles.fieldGroup}>
                    <label style={styles.label}>Количество ядер CPU:</label>
                    <input
                        type="number"
                        value={cpu}
                        onChange={(e) => setCpu(e.target.value)}
                        style={styles.input}
                        placeholder={initialCpu}
                        min={1}
                    />
                </div>

                <div style={styles.fieldGroup}>
                    <label style={styles.label}>Оперативная память (MB):</label>
                    <input
                        type="number"
                        value={ram}
                        onChange={(e) => setRam(e.target.value)}
                        style={styles.input}
                        placeholder={initialRam}
                        min={256}
                    />
                </div>

                <div style={styles.fieldGroup}>
                    <label style={styles.label}>Размер диска (GB):</label>
                    <input
                        type="number"
                        value={disk}
                        onChange={(e) => setDisk(e.target.value)}
                        style={styles.input}
                        placeholder={initialDisk}
                        min={1}
                    />
                </div>

                <div style={styles.fieldGroup}>
                    <label style={styles.label}>Путь до VirtualBox:</label>
                    <input
                        type="text"
                        value={vboxPath}
                        onChange={(e) => setVBoxPath(e.target.value)}
                        style={styles.input}
                        placeholder={initialVBoxPath || "C:\\Program Files\\Oracle\\VirtualBox\\VBoxManage.exe"}
                    />
                </div>

                <div style={styles.buttonContainer}>
                    <button onClick={saveSettings} style={styles.saveButton}>Сохранить</button>
                    <button onClick={resetSettings} style={styles.resetButton}>Сбросить</button>
                </div>
            </div>

            {statusMessage && <p style={styles.statusMessage}>{statusMessage}</p>}
        </div>
    );
};

const styles = {
    pageContainer: {
        maxWidth: "600px",
        margin: "0 auto",
        padding: "20px",
        fontFamily: "Segoe UI, sans-serif"
    },
    header: {
        textAlign: "center",
        marginBottom: "20px"
    },
    settingsForm: {
        display: "flex",
        flexDirection: "column",
        gap: "15px",
        backgroundColor: "#f1f1f1",
        padding: "20px",
        borderRadius: "12px",
        boxShadow: "0 0 10px rgba(0,0,0,0.05)"
    },
    fieldGroup: {
        display: "flex",
        flexDirection: "column"
    },
    label: {
        marginBottom: "5px",
        fontWeight: "bold"
    },
    input: {
        padding: "8px",
        border: "1px solid #ccc",
        borderRadius: "5px"
    },
    buttonContainer: {
        display: "flex",
        justifyContent: "space-between",
        marginTop: "10px"
    },
    saveButton: {
        backgroundColor: "#28a745",
        color: "white",
        border: "none",
        padding: "10px 16px",
        cursor: "pointer",
        borderRadius: "5px"
    },
    resetButton: {
        backgroundColor: "#dc3545",
        color: "white",
        border: "none",
        padding: "10px 16px",
        cursor: "pointer",
        borderRadius: "5px"
    },
    statusMessage: {
        marginTop: "15px",
        textAlign: "center",
        fontWeight: "bold",
        color: "#333"
    }
};

export default SettingsPage;
