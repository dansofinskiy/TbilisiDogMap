# Tbilisi Dog Map

Стартовая full-stack заготовка для сайта с картой Тбилиси, геометками фотографий собак и AI-описаниями.

## Что уже есть

- статический frontend в `/frontend`
- backend на `Kotlin + Spring Boot + Maven`
- PostgreSQL в Docker + Liquibase
- Dockerfile для Render
- карта на `MapLibre GL JS`
- тайлы OpenStreetMap
- кластеризация точек
- popup-карточка фотографии
- переключение языков `ka/en/ru`
- загрузка точек из Kotlin API

## Frontend

Фронтенд пока остается статическим и может открываться как обычный сайт:

1. открыть `/Users/d.sofinskii/Documents/New project/frontend/index.html` в браузере
2. либо поднять любой простой static server

По умолчанию фронт делает запрос в `http://localhost:8080/api/map/photos`.

Для production на GitHub Pages можно переопределить `frontend/config.js`:

```js
window.__APP_CONFIG__ = {
  API_BASE_URL: "https://your-render-service.onrender.com",
};
```

## Backend

Запуск backend:

```bash
mvn -s maven-settings.xml -Dmaven.repo.local=.m2 spring-boot:run
```

Проверка сборки:

```bash
mvn -s maven-settings.xml -Dmaven.repo.local=.m2 test
```

Текущий endpoint:

- `GET /api/map/photos` - только геометки
- `GET /api/photos/{id}` - полная карточка фотографии
- `POST /api/telegram/webhook` - intake webhook для Telegram-бота

Текущая БД по умолчанию:

- host: `localhost`
- port: `5432`
- database: `tbilisi_dog_map`
- user: `my_user`

Telegram bot:

- env: `TELEGRAM_BOT_TOKEN`
- webhook: `/api/telegram/webhook`
- flow: бот ждет фото и геолокацию в любом порядке, сохраняет draft, создает submission и сразу проводит его через backend-процессинг
- submission statuses: `NEW -> PROCESSING -> PUBLISHED` или `FAILED`

Render-ready env vars:

- `PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `TELEGRAM_BOT_TOKEN`
- `CORS_ALLOWED_ORIGIN_PATTERNS`

## Следующие шаги

- заменить мок-сервис на PostgreSQL/PostGIS
- добавить `GET /api/photos/{id}`
- подключить Telegram ingestion flow
- добавить модерацию и статусы публикации
