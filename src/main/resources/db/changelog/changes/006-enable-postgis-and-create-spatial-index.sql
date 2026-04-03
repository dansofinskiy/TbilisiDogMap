--liquibase formatted sql

--changeset codex:006-enable-postgis
create extension if not exists postgis;

--changeset codex:006-create-photos-spatial-index
create index if not exists idx_photos_location_gist
    on photos
    using gist (st_setsrid(st_makepoint(longitude, latitude), 4326));
