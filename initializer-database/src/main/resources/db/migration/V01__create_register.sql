create schema register;

CREATE TABLE register.receipt
(
    uuid            uuid primary key,
    qr_string       varchar(100) unique         not null,
    time            timestamp without time zone not null,
    sum             bigint                      not null,
    fiscal_number   bigint                      not null,
    fiscal_document bigint                      not null,
    fiscal_feature  bigint                      not null,
    create_at       timestamp with time zone    not null
);

CREATE TABLE register.manual
(
    uuid      uuid primary key,
    data      varchar(100)             not null,
    sum       int                      not null,
    create_at timestamp with time zone not null
);