--liquibase formatted sql

--changeset codex:004-alter-telegram-submissions
alter table telegram_submissions
    add column if not exists photo_id varchar(100),
    add column if not exists error_message text,
    add column if not exists processed_at timestamp with time zone;

--changeset codex:004-alter-photos
alter table photos
    add column if not exists source_submission_id varchar(100),
    add column if not exists telegram_photo_file_id varchar(255),
    add column if not exists telegram_photo_unique_id varchar(255);

--changeset codex:004-create-telegram-photo-indexes
create index if not exists idx_photos_source_submission_id
    on photos (source_submission_id);
