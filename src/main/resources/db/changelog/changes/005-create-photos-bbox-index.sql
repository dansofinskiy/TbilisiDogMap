--liquibase formatted sql

--changeset codex:005-create-photos-bbox-index
create index if not exists idx_photos_status_longitude_latitude_created_at
    on photos (status, longitude, latitude, created_at desc);
