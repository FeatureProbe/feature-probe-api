create table change_log
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `client_sdk_key` varchar(128) NOT NULL,
    `server_sdk_key` varchar(128) NOT NULL,
    `type`           varchar(128) NOT NULL,
    PRIMARY KEY (`id`)
)ENGINE=InnoDB collate = utf8mb4_unicode_ci;
alter table environment
    add version bigint default 1 not null after `deleted`;
INSERT INTO feature_probe.dictionary (value, `key`, account, deleted, modified_time, created_by, created_time,
                                      modified_by)
VALUES ('1', 'all_sdk_key_map', '', 0, '2022-10-18 14:50:36', -1, '2022-10-18 14:50:44', -1);