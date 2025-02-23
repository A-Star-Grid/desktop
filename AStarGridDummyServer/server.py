from flask import Flask, request, jsonify, send_file
import jwt
import datetime
import os
from functools import wraps
from flasgger import Swagger

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
USER = {'username': 'test_user', 'password': 'password'}

# Хранилище refresh токенов
REFRESH_TOKENS = {}

# Dummy projects
PROJECTS = [
    {"id": 1, "name": "Project A", "description": "Description A", "image": "image1.png", "reward": 100},
    {"id": 2, "name": "Project B", "description": "Description B", "image": "image2.png", "reward": 200},
    {"id": 3, "name": "Project C", "description": "Description C", "image": "image3.png", "reward": 300},
    {"id": 4, "name": "Project D", "description": "Description D", "image": "image4.png", "reward": 400},
    {"id": 5, "name": "Project E", "description": "Description E", "image": "image5.png", "reward": 500},
    {"id": 6, "name": "Project F", "description": "Description F", "image": "image6.png", "reward": 600},
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


@app.route('/projects', methods=['GET'])
@token_required
def get_projects(decoded_token):
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
        "per_page": per_page,
        "total_pages": total_pages,
        "projects": paginated_projects
    }), 200



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
    if data and data.get('username') == USER['username'] and data.get('password') == USER['password']:
        access_token = generate_access_token(USER['username'])
        refresh_token = generate_refresh_token(USER['username'])

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
      - name: deviceUuid
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



@app.route('/tasks/<int:project_id>', methods=['GET'])
@token_required
def get_tasks(decoded_token, project_id):
    """Get tasks for a project
    ---
    security:
      - Bearer: []
    parameters:
      - name: project_id
        in: path
        required: true
        type: integer
    responses:
      200:
        description: Task details
    """
    if project_id in TASKS:
        return jsonify(TASKS[project_id])
    return jsonify({'message': 'Project not found'}), 404

@app.route('/download/<int:project_id>', methods=['GET'])
@token_required
def download_data(decoded_token, project_id):
    """Download task dataset
    ---
    security:
      - Bearer: []
    parameters:
      - name: project_id
        in: path
        required: true
        type: integer
    responses:
      200:
        description: File download
    """
    filename = f'data_project_{project_id}.zip'
    filepath = os.path.join('data', filename)
    if os.path.exists(filepath):
        return send_file(filepath, as_attachment=True)
    return jsonify({'message': 'File not found'}), 404


@app.route('/user', methods=['GET'])
@token_required
def get_user(decoded_token):
    """Get user info from JWT
    ---
    security:
      - Bearer: []
    responses:
      200:
        description: User info
    """
    return jsonify({'username': decoded_token['user']}), 200

if __name__ == '__main__':
    os.makedirs('data', exist_ok=True)
    os.makedirs('results', exist_ok=True)
    app.run(host='0.0.0.0', port=5000, debug=True)