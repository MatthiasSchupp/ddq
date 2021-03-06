create table if not exists error
(
    id                bigint generated by default as identity,
    exception_message varchar(255),
    first_occurrence  timestamp    not null,
    last_occurrence   timestamp    not null,
    message           varchar(255) not null,
    occurrences       integer      not null,
    source            varchar(255) not null,
    stack_trace       varchar(2000),
    type              varchar(255) not null,
    hash              varchar(255) not null unique,
    primary key (id)
);

create table if not exists event_source
(
    name    varchar(255)  not null,
    last_id bigint,
    uri     varchar(1000) not null,
    version bigint,
    primary key (name)
);

create table if not exists event_store
(
    id                   bigint generated by default as identity,
    event                varchar(4000) not null,
    event_id             uuid          not null unique,
    name                 varchar(255)  not null,
    type                 varchar(255)  not null,
    source               varchar(255)  not null,
    timestamp            timestamp     not null,
    event_group          varchar(255),
    processing_instance  uuid,
    processing_status    varchar(255)  not null,
    processing_timestamp timestamp,
    version              integer,
    primary key (id)
);