create schema proverkacheka;

CREATE TABLE proverkacheka.receipt
(
    uuid                   uuid primary key,
    user_name              text                        not null,
    user_inn               varchar(12)                 not null,
    retail_place_name      text                        not null,
    retail_place_address   text                        not null,
    date_time              timestamp without time zone not null,
    total_sum              bigint                      not null,
    fiscal_drive_number    bigint                      not null,
    fiscal_document_number bigint                      not null,
    fiscal_sign            bigint                      not null,
    operation_type         int                         not null,
    shift_number           int                         not null,
    operator               text                        not null,
    request_number         int                         not null,
    fns_url                text                        not null,
    kkt_reg_id             text                        not null,
    credit_sum             bigint                      not null,
    prepaid_sum            bigint                      not null,
    cash_total_sum         bigint                      not null,
    provision_sum          bigint                      not null,
    ecash_total_sum        bigint                      not null,
    metadata               jsonb                       not null,
    create_at              timestamp with time zone    not null,
    response               jsonb                       not null
);

CREATE TABLE proverkacheka.items
(
    uuid       uuid primary key,
    receipt_id uuid    not null REFERENCES proverkacheka.receipt (uuid),
    name       text    not null,
    price      bigint  not null,
    quantity   decimal not null,
    sum        bigint  not null
);
