CREATE TABLE storage
(
    id    bigserial primary key not null,
    key   text                  not null unique,
    value text                  not null
);

