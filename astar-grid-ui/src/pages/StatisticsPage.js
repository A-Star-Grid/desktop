import React, { useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from "recharts";

const StatisticsPage = () => {
    const [statistics, setStatistics] = useState(null);
    const [projects, setProjects] = useState({});

    useEffect(() => {
        // Загружаем статистику
        fetch("http://localhost:8082/statistic/get_by_weak")
            .then(response => response.json())
            .then(data => setStatistics(data.statistics))
            .catch(error => console.error("Ошибка загрузки статистики:", error));

        // Загружаем список проектов
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

    if (!statistics || Object.keys(projects).length === 0) {
        return <p style={{ textAlign: "center", padding: "20px" }}>Загрузка статистики...</p>;
    }

    return (
        <div style={{ padding: "20px", maxWidth: "1000px", margin: "0 auto" }}>
            <h2 style={{ textAlign: "center", marginBottom: "30px" }}>Моя статистика</h2>
            {Object.entries(statistics).map(([device, data]) => {
                const formattedData = Object.entries(data.tasks).map(([projectId, count]) => ({
                    projectId,
                    projectName: projects[projectId] ? projects[projectId].name : `Проект ${projectId}`,
                    count,
                    totalReward: projects[projectId] ? count * projects[projectId].reward : 0
                }));

                return (
                    <div 
                        key={device} 
                        style={{ 
                            marginBottom: "60px", 
                            padding: "30px", 
                            border: "1px solid #ddd", 
                            borderRadius: "8px", 
                            background: "#f9f9f9",
                            boxShadow: "0px 4px 10px rgba(0,0,0,0.1)"
                        }}
                    >
                        <h3 style={{ textAlign: "center", marginBottom: "20px" }}>Устройство: {device}</h3>

                        {/* График количества задач */}
                        <div style={{ marginBottom: "50px", padding: "20px", background: "#fff", borderRadius: "8px" }}>
                            <h4 style={{ textAlign: "center", marginBottom: "15px" }}>Количество выполненных задач</h4>
                            <ResponsiveContainer width="100%" height={350}>
                                <BarChart data={formattedData} margin={{ top: 20, right: 30, left: 10, bottom: 60 }}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis 
                                        dataKey="projectName" 
                                        angle={-25} 
                                        textAnchor="end" 
                                        height={80} 
                                        interval={0} // Гарантирует, что все подписи проектов отображаются
                                    />
                                    <YAxis label={{ value: "Задачи", angle: -90, position: "insideLeft" }} />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="count" fill="#FF8C00" name="Количество задач" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>

                        {/* График общей суммы наград */}
                        <div style={{ padding: "20px", background: "#fff", borderRadius: "8px" }}>
                            <h4 style={{ textAlign: "center", marginBottom: "15px" }}>Общая сумма наград (руб.)</h4>
                            <ResponsiveContainer width="100%" height={350}>
                                <BarChart data={formattedData} margin={{ top: 20, right: 30, left: 10, bottom: 60 }}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis 
                                        dataKey="projectName" 
                                        angle={-25} 
                                        textAnchor="end" 
                                        height={80} 
                                        interval={0} 
                                    />
                                    <YAxis label={{ value: "Сумма награды", angle: -90, position: "insideLeft" }} />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="totalReward" fill="#FFA500" name="Сумма награды" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default StatisticsPage;
