create table targeting_version
(
    id             int auto_increment primary key,
    targeting_id   bigint            not null,
    project_key    varchar(128)      not null,
    environment_key varchar(128)      not null,
    comment        varchar(1024)     not null,
    content        text              not null,
    version        bigint            not null,
    deleted        tinyint default 0 not null,
    modified_time  datetime          not null,
    created_by     varchar(32)       not null,
    created_time   datetime          not null,
    modified_by    varchar(32)       not null
) collate = utf8mb4_unicode_ci;