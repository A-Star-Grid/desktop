import React, { useState } from "react";
import { BrowserRouter as Router, Route, Routes, useLocation } from "react-router-dom";
import Header from "./components/Header";
import LoginPage from "./pages/LoginPage";
import ProjectsPage from "./pages/ProjectsPage";
import SubscriptionsPage from "./pages/SubscriptionsPage";
import SettingsPage from "./pages/SettingsPage";
import StatisticsPage from "./pages/StatisticsPage";

function App() {
    const [username, setUsername] = useState("");

    return (
        <Router>
            <MainContent username={username} setUsername={setUsername} />
        </Router>
    );
}

function MainContent({ username, setUsername }) {
    const location = useLocation(); // Получаем текущий путь

    return (
        <>
            {/* Показываем Header, только если мы НЕ на странице входа */}
            {location.pathname !== "/" && <Header username={username} />}

            <Routes>
                <Route path="/" element={<LoginPage setUsername={setUsername} />} />
                <Route path="/projects" element={<ProjectsPage />} />
                <Route path="/subscriptions" element={<SubscriptionsPage />} />
                <Route path="/settings" element={<SettingsPage />} />
                <Route path="/statistics" element={<StatisticsPage />} />
            </Routes>
        </>
    );
}

export default App;
