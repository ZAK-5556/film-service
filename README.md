# Film Service — Блок 2: Spring Boot and REST API

REST-сервіс для фільмів і режисерів. Дані зберігаються в PostgreSQL, схема та довідник
режисерів створюються Liquibase під час запуску.

Стек: Java 21, Spring Boot 3.5, Spring Data JPA, Bean Validation, Liquibase, PostgreSQL.
Інтеграційні тести: Spring Boot Test + MockMvc на H2 у режимі PostgreSQL (Docker для тестів не потрібен).

## Предметна область

| Сутність | Поля | Зв'язок |
|----------|------|---------|
| **Film** (основна) | `id`, `title`, `year`, `country`, `genres` (жанри через кому), `director` | many-to-one → Director |
| **Director** | `id`, `name` (унікальне, без урахування регістру) | |

## Запуск

1. Запустити PostgreSQL (є готовий `docker-compose.yml`):
   ```bash
   docker compose up -d
   ```
   Або використати власний PostgreSQL: база `films`, користувач/пароль `films`/`films`.
   Параметри можна перевизначити змінними середовища `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. Зібрати та запустити сервіс:
   ```bash
   mvn clean package
   java -jar target/film-service-1.0.0.jar
   ```
   або `mvn spring-boot:run`. Сервіс доступний на `http://localhost:8080`.
3. Імпортувати тестові дані:
   ```bash
   curl -F "file=@data/films_import.json" http://localhost:8080/api/film/upload
   ```

Тести: `mvn test`.

## API

| Метод і шлях | Опис |
|--------------|------|
| `POST /api/film` | Створює фільм. Тіло: `{"title", "directorId", "year", "country", "genres"}`. `title`, `directorId`, `year` (1888–2100) обов'язкові. |
| `GET /api/film/{id}` | Деталі фільму разом з режисером: `"director": {"id": 1, "name": "..."}` |
| `PUT /api/film/{id}` | Оновлює фільм (та сама валідація, що й при створенні) |
| `DELETE /api/film/{id}` | Видаляє фільм |
| `POST /api/film/_list` | Сторінка списку: `{"directorId", "year", "country", "page", "size"}` → `{"list": [...], "totalPages": N}`. `page` нумерується з 0 (за замовчуванням 0), `size` 1–100 (за замовчуванням 20). Усі фільтри необов'язкові. |
| `POST /api/film/_report` | CSV-файл `films_report.csv` з усіма фільмами, що відповідають фільтру `{"directorId", "year", "country"}` |
| `POST /api/film/upload` | Імпорт JSON-файлу (multipart, поле `file`) → `{"imported", "failed", "errors"}` |
| `GET /api/director` | Усі режисери (за алфавітом) |
| `POST /api/director` | Створює режисера `{"name"}`; дублікат назви → `409 Conflict` |
| `PUT /api/director/{id}` | Перейменовує режисера з тим самим контролем унікальності |
| `DELETE /api/director/{id}` | Видаляє режисера; якщо в нього є фільми → `409 Conflict` |

Помилки повертаються у вигляді `{"message": "...", "fieldErrors": {"field": "..."}}`
зі статусами `400` (валідація, неіснуючий режисер у фільмі, некоректний JSON), `404`, `409`.

### Фільтрація та індекси

Фільтри `_list` і `_report` будуються через JPA Specification, тобто виконуються в SQL-запиті до БД.
Кожне поле фільтра має індекс (Liquibase changeSet `3-film-filter-indexes`):
`idx_film_director_id`, `idx_film_release_year`, `idx_film_country`.
Режисер підтягується тим самим запитом (`@EntityGraph`), без N+1.

Звіт формується порціями по 1000 записів і пишеться одразу у відповідь, тому весь результат
не завантажується в пам'ять.

## Імпорт

Формат файлу — як у Блоці 1 (`director` задається іменем, `genres` — рядком через кому або масивом):

```json
[
  {"title": "Inception", "director": "Christopher Nolan", "year": 2010, "country": "USA", "genres": "Sci-Fi, Thriller"}
]
```

Файл `data/films_import.json` узгоджений з Liquibase-наповненням режисерів: 15 записів імпортуються,
2 навмисно невалідні (режисера `Park Chan-wook` немає в довіднику; запис без `title`), тому відповідь буде:

```json
{"imported": 15, "failed": 2, "errors": ["Record 16: director 'Park Chan-wook' not found", "Record 17: title must not be blank"]}
```

Невалідний JSON (а не окремий запис) відхиляється повністю з кодом `400`, нічого не зберігається.

## CORS

Для фронтенду (Блок 3) дозволені запити з `http://localhost:3000` і `http://localhost:3050`
(змінна `CORS_ALLOWED_ORIGINS`).
