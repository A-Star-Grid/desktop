from flask import Flask, request, jsonify, send_file
import jwt
import datetime
import os
from functools import wraps
from flasgger import Swagger
import json
from collections import defaultdict
import datetime

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your_secret_key'
app.config['REFRESH_SECRET_KEY'] = 'your_refresh_secret_key'  # Отдельный ключ для refreshToken

swagger_template = {
    "swagger": "2.0",
    "info": {
        "title": "A*Grid Dummy Server",
        "description": "Dummy Flask Server for JWT Authentication",
        "version": "1.0.0"
    },
    "securityDefinitions": {
        "Bearer": {
            "type": "apiKey",
            "name": "Authorization",
            "in": "header",
            "description": "Enter your JWT token like this: Bearer <token>"
        }
    }
}

app.config['SWAGGER'] = {"uiversion": 3}
swagger = Swagger(app, template=swagger_template)

# Dummy user
USERS = {
    "test_user": {
        "password": "password",
        "email": "test@example.com",
        "balance": 0  # Начальный баланс
    }
}

# Хранилище refresh токенов
REFRESH_TOKENS = {}

# Dummy projects
PROJECTS = [
    {"id": 1, "name": "Project of Biba and Boba", "description": "Is a project of implement LLM for jocks", "image": "image1.png", "reward": 100},
    {"id": 2, "name": "Pupa-GPT", "description": "Project that realize GPT model in Rassian lang", "image": "image2.png", "reward": 200},
    {"id": 3, "name": "Lupa-GPT", "description": "Project without Pupa", "image": "image3.png", "reward": 300},
    {"id": 4, "name": "Detulie AI", "description": "Is a AI that coping maniers of John Detulie", "image": "image4.png", "reward": 400},
    {"id": 5, "name": "OOO Roga and Copita", "description": "Collect roga and copita", "image": "image5.png", "reward": 500},
    {"id": 6, "name": "AOA The Best Project", "description": "Project about best practics in programming", "image": "image6.png", "reward": 600},
]

SUBSCRIPTIONS = {}
TASKS = {
    1: {"code": "print('Task for Project A')", "data_url": "/download/1"},
    2: {"code": "print('Task for Project B')", "data_url": "/download/2"},
    3: {"code": "print('Task for Project C')", "data_url": "/download/3"},
    4: {"code": "print('Task for Project D')", "data_url": "/download/4"},
    5: {"code": "print('Task for Project E')", "data_url": "/download/5"},
    6: {"code": "print('Task for Project F')", "data_url": "/download/6"},
}

# Функция для создания accessToken
def generate_access_token(username):
    return jwt.encode(
        {'user': username, 'exp': datetime.datetime.utcnow() + datetime.timedelta(minutes=10)},
        app.config['SECRET_KEY'],
        algorithm='HS256'
    )

def generate_refresh_token(username):
    token = jwt.encode(
        {'user': username, 'exp': datetime.datetime.utcnow() + datetime.timedelta(days=7)},
        app.config['REFRESH_SECRET_KEY'],
        algorithm='HS256'
    )
    REFRESH_TOKENS[username] = token  # Сохраняем refreshToken
    return token



def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'message': 'Token is missing'}), 401
        try:
            token = token.split(" ")[1]  # Убираем "Bearer "
            decoded = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
            return f(decoded, *args, **kwargs)
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Invalid token'}), 401
    return decorated


# Декоратор проверки refreshToken
def refresh_token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        data = request.get_json()
        token = data.get('refreshToken')

        if not token:
            return jsonify({'message': 'Refresh token is missing'}), 401

        try:
            decoded = jwt.decode(token, app.config['REFRESH_SECRET_KEY'], algorithms=['HS256'])
            username = decoded['user']

            # Проверяем, совпадает ли refreshToken
            if REFRESH_TOKENS.get(username) != token:
                return jsonify({'message': 'Invalid refresh token'}), 401

            return f(decoded, *args, **kwargs)
        except jwt.ExpiredSignatureError:
            return jsonify({'message': 'Refresh token has expired'}), 401
        except jwt.InvalidTokenError:
            return jsonify({'message': 'Invalid refresh token'}), 401
    return decorated


@app.route('/refresh', methods=['POST'])
@refresh_token_required
def refresh_token(decoded_token):  # изменено имя функции
    """Refresh access token
    ---
    parameters:
      - name: refreshToken
        in: body
        required: true
        schema:
          type: object
          properties:
            refreshToken:
              type: string
    responses:
      200:
        description: New access token returned
    """
    username = decoded_token['user']
    new_access_token = generate_access_token(username)
    new_refresh_token = generate_refresh_token(username)
    return jsonify({'accessToken': new_access_token, 'refreshToken': new_refresh_token})


@app.route('/projects_paginate', methods=['GET'])
@token_required
def get_projects_paginate(decoded_token):
    """Get paginated list of projects
    ---
    security:
      - Bearer: []
    parameters:
      - name: page
        in: query
        type: integer
        required: false
        default: 1
        description: Page number
      - name: per_page
        in: query
        type: integer
        required: false
        default: 3
        description: Number of projects per page
    responses:
      200:
        description: List of paginated projects
        schema:
          type: object
          properties:
            total:
              type: integer
              description: Total number of projects
            page:
              type: integer
              description: Current page
            per_page:
              type: integer
              description: Number of projects per page
            total_pages:
              type: integer
              description: Total number of pages
            projects:
              type: array
              items:
                type: object
                properties:
                  id:
                    type: integer
                  name:
                    type: string
                  description:
                    type: string
                  image:
                    type: string
                  reward:
                    type: integer
    """
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 3, type=int)
    
    total_projects = len(PROJECTS)
    total_pages = (total_projects + per_page - 1) // per_page  # Вычисляем общее число страниц

    start = (page - 1) * per_page
    end = start + per_page
    paginated_projects = PROJECTS[start:end]

    return jsonify({
        "total": total_projects,
        "page": page,
        "perPage": per_page,
        "totalPages": total_pages,
        "projects": paginated_projects
    }), 200


@app.route('/projects', methods=['GET'])
@token_required
def get_projects(decoded_token):
    """Get paginated list of projects
    ---
    security:
      - Bearer: []
    responses:
      200:
        description: List of paginated projects
        schema:
	  type: object
          properties:
            projects:
              type: array
              items:
                type: object
                properties:
                  id:
                    type: integer
                  name:
                    type: string
                  description:
                    type: string
                  image:
                    type: string
                  reward:
                    type: integer
    """
    #page = request.args.get('page', 1, type=int)
    #per_page = request.args.get('per_page', 3, type=int)

    #total_projects = len(PROJECTS)
    #total_pages = (total_projects + per_page - 1) // per_page  # Вычисляем общее число страниц

    #start = (page - 1) * per_page
    #end = start + per_page
    # paginated_projects = PROJECTS[start:end]

    return jsonify({'projects': PROJECTS}), 200


@app.route('/login', methods=['POST'])
def login():
    """User login
    ---
    parameters:
      - name: username
        in: body
        required: true
        schema:
          type: object
          properties:
            username:
              type: string
            password:
              type: string
    responses:
      200:
        description: JWT tokens returned
    """
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if username in USERS and USERS[username]['password'] == password:
        access_token = generate_access_token(username)
        refresh_token = generate_refresh_token(username)
        return jsonify({'accessToken': access_token, 'refreshToken': refresh_token})
    
    return jsonify({'message': 'Invalid credentials'}), 401




@app.route('/logout', methods=['POST'])
@refresh_token_required
def logout(decoded_token):
    """Logout user (invalidate refresh token)
    ---
    parameters:
      - name: refreshToken
        in: body
        required: true
        schema:
          type: object
          properties:
            refreshToken:
              type: string
    responses:
      200:
        description: User logged out
    """
    username = decoded_token['user']
    REFRESH_TOKENS.pop(username, None)  # Удаляем refreshToken
    return jsonify({'message': 'Logged out successfully'}), 200




# Дополнительное хранилище подписок с параметрами
SUBSCRIPTIONS_DETAILS = {}

@app.route('/subscribe', methods=['POST'])
@token_required
def subscribe(decoded_token):
    """
    Subscribe to a project with schedule intervals and resource allocation
    ---
    security:
      - Bearer: []
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            deviceUuid:
              type: string
              description: Unique identifier of the device
            projectId:
              type: integer
              description: ID of the project
            scheduleIntervals:
              type: array
              items:
                type: object
                properties:
                  start:
                    type: object
                    properties:
                      day:
                        type: string
                      time:
                        type: integer
                  end:
                    type: object
                    properties:
                      day:
                        type: string
                      time:
                        type: integer
                  computeResource:
                    type: object
                    properties:
                      cpuCores:
                        type: integer
                      diskSpace:
                        type: integer
                      ram:
                        type: integer
    responses:
      200: 
        description: Subscription successful
    """

    data = request.get_json()
    
    device_uuid = data.get("deviceUuid")
    project_id = data.get("projectId")
    schedule_intervals = data.get("scheduleIntervals")

    if not device_uuid:
        return jsonify({'message': 'Device UUID is required'}), 400
    if project_id is None:
        return jsonify({'message': 'Project ID is required'}), 400
    if not schedule_intervals:
        return jsonify({'message': 'Schedule intervals are required'}), 400

    user = decoded_token['user']

    # Добавляем подписку на конкретный проект
    SUBSCRIPTIONS_DETAILS.setdefault(user, {}).setdefault(device_uuid, {}).setdefault(project_id, []).extend(schedule_intervals)

    return jsonify({'message': f'Subscribed to project {project_id} on device {device_uuid}'}), 200


@app.route('/subscriptions', methods=['GET'])
@token_required
def get_subscriptions(decoded_token):
    """
    Get all subscriptions for a specific device
    ---
    security:
      - Bearer: []
    parameters:
      - name: device_uuid
        in: query
        required: true
        type: string
        description: Unique identifier of the device
    responses:
      200:
        description: List of subscriptions
        schema:
          type: array
          items:
            type: object
            properties:
              projectId:
                type: integer
              scheduleIntervals:
                type: array
                items:
                  type: object
                  properties:
                    start:
                      type: object
                      properties:
                        day:
                          type: string
                        time:
                          type: integer
                    end:
                      type: object
                      properties:
                        day:
                          type: string
                        time:
                          type: integer
                    computeResource:
                      type: object
                      properties:
                        cpuCores:
                          type: integer
                        diskSpace:
                          type: integer
                        ram:
                          type: integer
    """
    user = decoded_token['user']
    device_uuid = request.args.get("device_uuid")

    if not device_uuid:
        return jsonify({'message': 'Device UUID is required'}), 400

    # Получаем подписки для пользователя и устройства
    user_subscriptions = SUBSCRIPTIONS_DETAILS.get(user, {}).get(device_uuid, {})

    # Преобразуем объект в список
    subscription_list = [
        {"projectId": project_id, "scheduleIntervals": schedule_intervals}
        for project_id, schedule_intervals in user_subscriptions.items()
    ]

    return jsonify(subscription_list), 200


@app.route('/unsubscribe', methods=['POST'])
@token_required
def unsubscribe(decoded_token):
    """
    Unsubscribe from a project by removing all associated schedule intervals
    ---
    security:
      - Bearer: []
    parameters:
      - name: deviceUuid
        in: query
        required: true
        type: string
        description: Unique identifier of the device
      - name: projectId
        in: query
        required: true
        type: integer
        description: ID of the project
    responses:
      200:
        description: Unsubscription successful
    """
    user = decoded_token['user']
    device_uuid = request.args.get("device_uuid")
    project_id = request.args.get("project_id", type=int)

    if not device_uuid:
        return jsonify({'message': 'Device UUID is required'}), 400
    if project_id is None:
        return jsonify({'message': 'Project ID is required'}), 400

    if user in SUBSCRIPTIONS_DETAILS and device_uuid in SUBSCRIPTIONS_DETAILS[user]:
        if project_id in SUBSCRIPTIONS_DETAILS[user][device_uuid]:
            del SUBSCRIPTIONS_DETAILS[user][device_uuid][project_id]
            return jsonify({'message': f'Unsubscribed from project {project_id} on device {device_uuid}'}), 200

    return jsonify({'message': 'Subscription not found'}), 404


@app.route('/user', methods=['GET'])
@token_required
def get_user(decoded_token):
    """Get user info
    ---
    security:
      - Bearer: []
    responses:
      200:
        description: User info with balance and email
    """
    username = decoded_token['user']
    user_data = USERS.get(username, {})
    return jsonify({
        'username': username,
        'email': user_data.get('email'),
        'balance': user_data.get('balance')
    }), 200


TASK_MAPPING = {
    1: "286da1e4-fbef-4243-b1ac-d385da123ee0",
    2: "53420001-ad7a-4681-b48b-a6704cb502b6",
    3: "11c3c920-1045-4fbf-95c7-48d30abbade1",
    4: "202d816a-054d-4252-bb4c-adcaa0aa7d88",
    5: "1d299e40-033f-490d-9d68-666a4c444f61",
    6: "e2631ea9-ea83-4256-adf5-1fab6f95e754"
}

@app.route('/get_current_task', methods=['GET'])
@token_required
def get_current_task(decoded_token):
    """
    Get current task UUID for project
    ---
    security:
      - Bearer: []
    parameters:
      - name: project_id
        in: query
        required: true
        type: integer
        description: ID of the project
      - name: device_uuid
        in: query
        required: true
        type: string
        description: Unique identifier of the device
    responses:
      200:
        description: Task UUID response
        schema:
          properties:
            taskUuid:
              type: string
      400:
        description: Missing parameters
      404:
        description: Task not found for project
    """
    project_id = request.args.get("project_id", type=int)
    device_uuid = request.args.get("device_uuid")

    if project_id is None or not device_uuid:
        return jsonify({'message': 'project_id and device_uuid are required'}), 400

    task_uuid = TASK_MAPPING.get(project_id)
    
    if not task_uuid:
        return jsonify({'message': 'Task not found for this project'}), 404

    return jsonify({'taskUuid': task_uuid}), 200


@app.route('/download', methods=['GET'])
@token_required
def download_task(decoded_token):
    """
    Download task archive
    ---
    security:
      - Bearer: []
    parameters:
      - name: task_uuid
        in: query
        required: true
        type: string
        description: UUID of the task
    responses:
      200:
        description: Task archive file
      400:
        description: Missing parameters
      404:
        description: File not found
    """
    task_uuid = request.args.get("task_uuid")

    if not task_uuid:
        return jsonify({'message': 'task_uuid is required'}), 400

    filename = f"{task_uuid}.zip"
    file_path = os.path.join("data", filename)

    if not os.path.exists(file_path):
        return jsonify({'message': 'File not found'}), 404

    return send_file(file_path, as_attachment=True)


@app.route('/upload_result', methods=['POST'])
@token_required
def upload_result(decoded_token):
    """Upload result archive and update balance
    ---
    security:
      - Bearer: []
    parameters:
      - name: task_uuid
        in: query
        required: true
        type: string
      - name: project_id
        in: query
        required: true
        type: integer
      - name: device_uuid
        in: query
        required: true
        type: string
      - name: file
        in: formData
        type: file
        required: true
    responses:
      200:
        description: File uploaded successfully, balance updated
      400:
        description: Missing parameters or file
    """
    task_uuid = request.args.get("task_uuid")
    project_id = request.args.get("project_id", type=int)
    device_uuid = request.args.get("device_uuid")
    file = request.files.get("file")

    if None in [task_uuid, project_id, device_uuid] or not file:
        return jsonify({'message': 'task_uuid, project_id, device_uuid and file are required'}), 400

    os.makedirs("results", exist_ok=True)
    save_path = os.path.join("results", f"{task_uuid}_{device_uuid}.zip")
    file.save(save_path)

    # Найти проект и его вознаграждение
    project = next((p for p in PROJECTS if p["id"] == project_id), None)
    if not project:
        return jsonify({'message': 'Project not found'}), 404

    reward = project["reward"]

    # Обновляем баланс пользователя
    username = decoded_token['user']
    USERS[username]['balance'] += reward

    # **Обновляем статистику пользователя**
    update_user_statistics(username, device_uuid, project_id)

    return jsonify({
        'message': f'Result for task {task_uuid} uploaded successfully',
        'reward': reward,
        'new_balance': USERS[username]['balance']
    }), 200


# Хранилище статистики (6 дней захардкожено, последний день обновляется динамически)
USER_STATISTICS = defaultdict(lambda: defaultdict(lambda: defaultdict(int)))

# Добавляем захардкоженные данные
HARD_CODED_STATISTICS = [
    {"date": (datetime.datetime.utcnow() - datetime.timedelta(days=i)).strftime('%Y-%m-%d'),
     "deviceUuid": "00000000-0000-0000-0000-000000000000",
     "tasks": {
         1: 2,  # 2 задачи выполнено по проекту 1
         2: 1,  # 1 задача по проекту 2
         3: 3   # 3 задачи по проекту 3
     }} for i in range(6)
]

@app.route('/user_statistics', methods=['GET'])
@token_required
def get_user_statistics(decoded_token):
    """
    Get user task statistics for the last 7 days
    ---
    security:
      - Bearer: []
    responses:
      200:
        description: User statistics including predefined data
        schema:
          type: object
          properties:
            username:
              type: string
            statistics:
              type: object
              additionalProperties:
                type: object
                properties:
                  tasks:
                    type: object
                    additionalProperties:
                      type: integer
                      description: Number of completed tasks per project
    """
    username = decoded_token['user']

    # Получаем динамическую статистику пользователя
    user_stats = USER_STATISTICS.get(username, {})

    # Преобразуем данные в нужный формат
    formatted_stats = {
        device_uuid: {
            "tasks": {project_id: project_tasks for project_id, project_tasks in projects.items()}
        }
        for device_uuid, projects in user_stats.items()
    }

    # Добавляем захардкоженные данные, если их нет
    if "00000000-0000-0000-0000-000000000000" not in formatted_stats:
        formatted_stats["00000000-0000-0000-0000-000000000000"] = {
            "tasks": {entry["id"]: entry["reward"] // 100 for entry in PROJECTS}  # Пример данных
        }

    return jsonify({
        "username": username,
        "statistics": formatted_stats
    }), 200



def update_user_statistics(username, device_uuid, project_id):
    """
    Update user statistics for completed tasks.
    """
    USER_STATISTICS[username][device_uuid][project_id] += 1


if __name__ == '__main__':
    os.makedirs('data', exist_ok=True)
    os.makedirs('results', exist_ok=True)
    app.run(host='0.0.0.0', port=5000, debug=True)




