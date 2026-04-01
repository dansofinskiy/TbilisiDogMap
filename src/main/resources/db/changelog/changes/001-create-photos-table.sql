--liquibase formatted sql

--changeset codex:001-create-photos-table
create table if not exists photos (
    id varchar(100) primary key,
    title varchar(255) not null,
    district varchar(255) not null,
    latitude double precision not null,
    longitude double precision not null,
    created_at timestamp with time zone not null,
    ai_description text not null,
    caption text not null,
    ai_confidence double precision not null,
    source varchar(50) not null,
    image_url text not null,
    status varchar(50) not null
);

--changeset codex:001-create-photos-index
create index if not exists idx_photos_status_created_at on photos (status, created_at desc);
