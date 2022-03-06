create schema statistic;

CREATE TABLE statistic.group
(
    id    serial primary key,
    label varchar(32) not null,
    color varchar(7)
);

CREATE TABLE statistic.group_word
(
    group_id int REFERENCES statistic.group (id),
    word     varchar(32) not null,
    CONSTRAINT constraint_name UNIQUE (group_id, word)
);


CREATE TABLE statistic.items
(
    uuid        uuid primary key,
    register_id uuid                        not null,
    name        text                        not null,
    price       bigint                      not null,
    quantity    decimal                     not null,
    sum         bigint                      not null,
    date_time   timestamp without time zone not null,
    group_id    integer REFERENCES statistic.group (id),
    create_at   timestamp with time zone    not null
);