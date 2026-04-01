--liquibase formatted sql

--changeset codex:003-create-telegram-submission-drafts
create table if not exists telegram_submission_drafts (
    chat_id bigint primary key,
    telegram_user_id bigint not null,
    username varchar(255),
    photo_file_id varchar(255),
    photo_unique_id varchar(255),
    caption text,
    latitude double precision,
    longitude double precision,
    updated_at timestamp with time zone not null default now()
);

--changeset codex:003-create-telegram-submissions
create table if not exists telegram_submissions (
    id varchar(100) primary key,
    chat_id bigint not null,
    telegram_user_id bigint not null,
    username varchar(255),
    photo_file_id varchar(255) not null,
    photo_unique_id varchar(255),
    caption text,
    latitude double precision not null,
    longitude double precision not null,
    status varchar(50) not null,
    created_at timestamp with time zone not null default now()
);

--changeset codex:003-create-telegram-submissions-indexes
create index if not exists idx_telegram_submissions_created_at
    on telegram_submissions (created_at desc);
