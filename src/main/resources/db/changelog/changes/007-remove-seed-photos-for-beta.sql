--liquibase formatted sql

--changeset codex:007-remove-seed-photos-for-beta
delete from photos
where id in (
    'mtatsminda-sun',
    'vera-green',
    'avlabari-stairs',
    'saburtalo-park'
);
