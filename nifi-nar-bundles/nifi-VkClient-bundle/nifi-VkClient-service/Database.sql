-- Prepared
CREATE SCHEMA IF NOT EXISTS vk;
CREATE TABLE IF NOT EXISTS vk.key_value_storage
(
    id    bigserial primary key not null,
    key   text                  not null unique,
    value text                  not null
);


-- V001__CreateVkCookie
create table vk.vk_cookie
(
    id          bigserial primary key not null,
    url         varchar               not null,
    name        varchar               not null,
    domain      varchar               not null,
    path        varchar               not null,
    value       varchar               not null,
    comment     varchar,
    comment_url varchar,
    discard     varchar,
    max_age     timestamp(3) with time zone,
    portlist    varchar,
    secure      boolean,
    http_only   boolean,
    version     integer,
    unique (name, domain, path)
);


