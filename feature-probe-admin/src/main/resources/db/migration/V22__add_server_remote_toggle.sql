INSERT INTO `toggle` (`organization_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, `variations`,
                      `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, `created_by`,
                      `created_time`, `modified_time`)
VALUES (-1,
        'Control FeatureProbe SDKs\' RemoteUrl', 'remote_url', 'Control the remoteUrl for SDKs\' to connect FeatureProbe Server',
        'string', 0,
        '[{\"value\":\"https://featureprobe.io/server\",\"name\":\"online service\",\"description\":\"\"},{\"value\":\"http://127.0.0.1:4007\",\"name\":\"local docker\",\"description\":\"\"}]',
        'feature_probe', 0, 1, 0, -1, -1, now(), now());

INSERT INTO `targeting` (`organization_id`, `toggle_key`, `environment_key`, `project_key`, `version`, `disabled`,
                         `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'remote_url', 'online', 'feature_probe', 1, 0,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"https://featureprobe.io/server\",\"name\":\"online service\",\"description\":\"\"},{\"value\":\"http://127.0.0.1:4007\",\"name\":\"local docker\",\"description\":\"\"}]}',
        0, -1, -1, now(), now());

INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (-1, 'feature_probe', 'remote_url', 'online', 1, 'http://127.0.0.1:4007', 1, 'local docker');

INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (-1, 'feature_probe', 'remote_url', 'online', 1, 'https://featureprobe.io/server', 0, 'online service');

INSERT INTO `targeting_version` (`organization_id`, `project_key`, `environment_key`, `toggle_key`, `comment`,
                                 `content`, `disabled`, `version`, `deleted`, `modified_time`, `created_by`,
                                 `created_time`, `modified_by`)
VALUES (-1, 'feature_probe', 'online', 'remote_url', '',
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"https://featureprobe.io/server\",\"name\":\"online service\",\"description\":\"\"},{\"value\":\"http://127.0.0.1:4007\",\"name\":\"local docker\",\"description\":\"\"}]}',
        0, 1, 0, now(), -1, now(), -1);
