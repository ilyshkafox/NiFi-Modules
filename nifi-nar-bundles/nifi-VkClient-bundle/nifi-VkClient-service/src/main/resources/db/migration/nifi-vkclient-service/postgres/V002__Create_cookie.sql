CREATE TABLE cookie
(
    id          bigserial primary key not null,
    url         varchar               not null,
    name        varchar               not null,
    domain      varchar               not null,
    path        varchar               not null,
    value       varchar               not null,
    comment     varchar,
    comment_url varchar,
    discard     bool,
    max_age     timestamp(3) with time zone,
    portlist    varchar,
    secure      boolean,
    http_only   boolean,
    version     integer,
    unique (name, domain, path)
);


