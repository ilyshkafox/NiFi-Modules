create schema cashback_vk;

CREATE TABLE cashback_vk.cookie
(
    id          bigserial primary key,
    uri         text,
    name        text not null,
    domain      text not null,
    path        text not null,
    value       text not null,
    comment     text,
    comment_url text,
    discard     text,
    max_age     timestamp with time zone,
    portlist    text,
    secure      bool,
    http_only   bool,
    version     int,
    UNIQUE (name, domain, path)
);
