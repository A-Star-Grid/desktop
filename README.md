# AStarGrid Desktop Computing Application

AStarGrid — это десктопное приложение для распределенных вычислений, разработанное на Java, JavaScript и Python. Проект позволяет запускать и управлять задачами вычислений через единый интерфейс, используя модульную архитектуру.

## Модули проекта

### [AStarGridCore](./AStarGridCore)
**Язык:** Java  
**Назначение:** Основная вычислительная логика и клиент взаимодействия с удаленными задачами.

#### Сборка и запуск
```bash
cd AStarGridCore
mvn clean package
java -jar target/AStarGridCore.jar
```

---

### [AStarGridDummyServer](./AStarGridDummyServer)
**Язык:** Python  
**Назначение:** Тестовый сервер, представляющий наивную реализацию функционала сервера.

#### Сборка и запуск
```bash
cd AStarGridDummyServer
python server.py
```

---

### [astar-grid-ui](./astar-grid-ui)
**Языки:** React, JavaScript  
**Назначение:** Графический интерфейс для пользователя, взаимодействующий с Core и DummyServer.

#### Установка зависимостей и запуск
```bash
cd astar-grid-ui
npm install
npm start
```

---

## Системные требования

- Java 17+
- Node.js 16+ (для UI)
- Maven
- Git

Соберите и запустите каждый модуль, следуя инструкциям выше.

## Авторы
Разработано командой AStarGrid.
