# Tbilisi Dog Map

Стартовая full-stack заготовка для сайта с картой Тбилиси, геометками фотографий собак и AI-описаниями.

## Что уже есть

- статический frontend в `/frontend`
- backend на `Kotlin + Spring Boot + Maven`
- PostgreSQL/PostGIS в Docker + Liquibase
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

Локальный Docker Compose запуск:

```bash
cp .env.example .env
docker compose up -d --build
```

По умолчанию compose использует уже существующую PostgreSQL на `localhost:5432` и поднимает только приложение.

Если когда-нибудь понадобится локальная БД внутри compose, можно запустить профиль `local-db`:

```bash
docker compose --profile local-db up -d --build
```

## Raspberry Pi + PostGIS

Для Raspberry Pi подготовлен отдельный compose-файл с образом `postgis/postgis:17-3.5-alpine`:

```bash
cp .env.rpi.example .env
docker compose -f docker-compose.rpi.yml up -d --build
```

Что делает этот сценарий:

- поднимает `postgis/postgis`
- создает обычный PostgreSQL database для приложения
- backend подключается к БД по внутреннему имени `db`
- Liquibase включает `postgis` и создает spatial index для геозапросов

Проверка:

```bash
curl "http://localhost:8080/api/map/photos?bbox=44.70,41.65,44.85,41.75"
```

Текущий endpoint:

- `GET /api/map/photos?bbox=minLng,minLat,maxLng,maxLat` - только геометки внутри прямоугольника
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

- поднять `docker-compose.rpi.yml` на Raspberry Pi
- подключить tunnel или домен для Telegram webhook
- добавить storage для оригиналов фотографий вместо временного URL из Telegram
- добавить модерацию и статусы публикации
