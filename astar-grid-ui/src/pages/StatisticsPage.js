import React, { useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from "recharts";

const StatisticsPage = () => {
    const [statistics, setStatistics] = useState(null);
    const [canceledStats, setCanceledStats] = useState(null);
    const [projects, setProjects] = useState({});

    useEffect(() => {
        fetch("http://localhost:8082/statistic/get_by_weak")
            .then(response => response.json())
            .then(data => setStatistics(data.statistics))
            .catch(error => console.error("Ошибка загрузки статистики:", error));

        fetch("http://localhost:8082/statistic/get_canceled_stat")
            .then(response => response.json())
            .then(data => {
                console.log("Отменённые задачи:", data.cancelledTasks);
                setCanceledStats(data.cancelledTasks);
            })
            .catch(error => console.error("Ошибка загрузки отменённых задач:", error));

        fetch("http://localhost:8082/project/list?page=1&perPage=100")
            .then(response => response.json())
            .then(data => {
                const projectsMap = {};
                data.projects.forEach(project => {
                    projectsMap[project.id] = { name: project.name, reward: project.reward };
                });
                setProjects(projectsMap);
            })
            .catch(error => console.error("Ошибка загрузки проектов:", error));
    }, []);

    if (!statistics || !canceledStats || Object.keys(projects).length === 0) {
        return <p style={{ textAlign: "center", padding: "20px" }}>Загрузка статистики...</p>;
    }

    const combinedProjectStats = Object.keys(projects).map(projectId => {
        const pid = Number(projectId);
        let completed = 0;
        let canceled = 0;

        Object.values(statistics).forEach(stat => {
            completed += stat.tasks[pid] || 0;
        });

        Object.values(canceledStats).forEach(deviceEntry => {
            if (deviceEntry[pid]) {
                canceled += deviceEntry[pid].length;
            }
        });

        return {
            projectName: projects[pid]?.name || `Проект ${pid}`,
            completed,
            canceled
        };
    });

    return (
        <div style={{ padding: "20px", maxWidth: "1400px", margin: "0 auto" }}>
            <h2 style={{ textAlign: "center", marginBottom: "30px" }}>Моя статистика</h2>

            {Object.entries(statistics).map(([device, data]) => {
                const formattedData = Object.entries(data.tasks).map(([projectId, count]) => ({
                    projectId,
                    projectName: projects[projectId] ? projects[projectId].name : `Проект ${projectId}`,
                    count,
                    totalReward: projects[projectId] ? count * projects[projectId].reward : 0
                }));

                const successful = Object.values(data.tasks).reduce((sum, count) => sum + count, 0);
                const failed = Object.values(canceledStats[device] || {}).reduce((sum, arr) => sum + arr.length, 0);

                return (
                    <div key={device} style={{ marginBottom: "60px", padding: "30px", border: "1px solid #ddd", borderRadius: "8px", background: "#f9f9f9", boxShadow: "0px 4px 10px rgba(0,0,0,0.1)" }}>
                        <h3 style={{ textAlign: "center", marginBottom: "30px" }}>Устройство: {device}</h3>

                        <div style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
                            gap: "30px"
                        }}>
                            {/* Выполненные задачи */}
                            <div style={{ background: "#fff", borderRadius: "8px", padding: "20px" }}>
                                <h4 style={{ textAlign: "center", marginBottom: "15px" }}>Количество выполненных задач</h4>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={formattedData} margin={{ top: 20, right: 30, left: 10, bottom: 60 }}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="projectName" angle={-25} textAnchor="end" height={80} interval={0} />
                                        <YAxis label={{ value: "Задачи", angle: -90, position: "insideLeft" }} />
                                        <Tooltip />
                                        <Legend />
                                        <Bar dataKey="count" fill="#FFA500" name="Количество задач" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>

                            {/* Сумма наград */}
                            <div style={{ background: "#fff", borderRadius: "8px", padding: "20px" }}>
                                <h4 style={{ textAlign: "center", marginBottom: "15px" }}>Общая сумма наград (руб.)</h4>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={formattedData} margin={{ top: 20, right: 30, left: 10, bottom: 60 }}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="projectName" angle={-25} textAnchor="end" height={80} interval={0} />
                                        <YAxis label={{ value: "Сумма награды", angle: -90, position: "insideLeft" }} />
                                        <Tooltip />
                                        <Legend />
                                        <Bar dataKey="totalReward" fill="#FFB347" name="Сумма награды" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>

                            {/* Успешные / Неуспешные */}
                            <div style={{ background: "#fff", borderRadius: "8px", padding: "20px" }}>
                                <h4 style={{ textAlign: "center", marginBottom: "15px" }}>Завершенные / Отмененные выполнения</h4>
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={[{ device, successful, failed }]} margin={{ top: 20, right: 30, left: 10, bottom: 20 }}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="device" />
                                        <YAxis label={{ value: "Количество задач", angle: -90, position: "insideLeft" }} />
                                        <Tooltip />
                                        <Legend />
                                        <Bar dataKey="successful" fill="#FFA500" name="Успешные" />
                                        <Bar dataKey="failed" fill="#FF6347" name="Неуспешные" />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    </div>
                );
            })}

            <div style={{ marginTop: "60px", padding: "30px", background: "#fff", borderRadius: "8px", boxShadow: "0px 4px 10px rgba(0,0,0,0.1)" }}>
                <h3 style={{ textAlign: "center", marginBottom: "20px" }}>Отменённые / Завершенные задачи по проектам</h3>
                <ResponsiveContainer width="100%" height={400}>
                    <BarChart data={combinedProjectStats} margin={{ top: 20, right: 30, left: 10, bottom: 60 }}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="projectName" angle={-25} textAnchor="end" height={80} interval={0} />
                        <YAxis label={{ value: "Задачи", angle: -90, position: "insideLeft" }} />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="completed" fill="#FFA500" name="Выполнено" />
                        <Bar dataKey="canceled" fill="#FF6347" name="Отменено" />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
};

export default StatisticsPage;
