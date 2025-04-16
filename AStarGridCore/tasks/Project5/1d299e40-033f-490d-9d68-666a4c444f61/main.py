import time
import os

# Создаем директорию, если её нет
output_dir = "output"
os.makedirs(output_dir, exist_ok=True)

# Определяем путь к файлу лога
log_file = os.path.join(output_dir, "out.log")

# Функция для записи в лог и в консоль
def log_message(message):
    print(message)  # Вывод в консоль
    with open(log_file, "a", encoding="utf-8") as f:
        f.write(message + "\n")  # Запись в файл

# Основная логика
log_message("Example task ranned!")
time.sleep(600)
log_message("Прошло 600 секунд!")
