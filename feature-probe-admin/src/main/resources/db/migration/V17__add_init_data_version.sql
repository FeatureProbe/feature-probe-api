INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'header_skin', 'online', 1, 'false', 0, 'Light skin');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'header_skin', 'online', 1, 'true', 1, 'Dark skin');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'header_skin', 'test', 1, 'false', 0, 'Light skin');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'header_skin', 'test', 1, 'true', 1, 'Dark skin');


INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'online', 'header_skin', 'init',
        '{"rules":[],"disabledServe":{"select":0},"defaultServe":{"select":0},"variations":[{"value":"false","name":"Light skin","description":"Use default skin."},{"value":"true","name":"Dark skin", "description":"Switch to Dark skin."}]}',
        0, 1, 0, now(), 1, now(), 1);
INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'test', 'header_skin', 'init',
        '{"rules":[],"disabledServe":{"select":0},"defaultServe":{"select":0},"variations":[{"value":"false","name":"Light skin","description":"Use default skin."},{"value":"true","name":"Dark skin", "description":"Switch to Dark skin."}]}',
        0, 1, 0, now(), 1, now(), 1);


INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'color_ab_test', 'online', 1, 'red', 0, 'Red Button');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'color_ab_test', 'online', 1, 'blue', 1, 'Blue Button');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'color_ab_test', 'test', 1, 'red', 0, 'Red Button');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'color_ab_test', 'test', 1, 'red', 1, 'Blue Button');


INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'online', 'color_ab_test', 'init',
        '{"rules":[{"conditions":[{"type":"string","subject":"city","predicate":"is one of","objects":["Paris"]},{"type":"string","subject":"gender","predicate":"is one of","objects":["famale"]}],"name":"Paris women show 50% red buttons, 50% blue","serve":{"split":[5000,5000,0]}}],"disabledServe":{"select":1},"defaultServe":{"select":1},"variations":[{"value":"red","name":"Red Button","description":"Set button color to Red"},{"value":"blue","name":"Blue Button","description":"Set button color to Blue"}]}',
        0, 1, 0, now(), 1, now(), 1);
INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'test', 'color_ab_test', 'init',
        '{"rules":[{"conditions":[{"type":"string","subject":"city","predicate":"is one of","objects":["Paris"]},{"type":"string","subject":"gender","predicate":"is one of","objects":["famale"]}],"name":"Paris women show 50% red buttons, 50% blue","serve":{"split":[5000,5000,0]}}],"disabledServe":{"select":1},"defaultServe":{"select":1},"variations":[{"value":"red","name":"Red Button","description":"Set button color to Red"},{"value":"blue","name":"Blue Button","description":"Set button color to Blue"}]}',
        0, 1, 0, now(), 1, now(), 1);



INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'online', 1, '10', 0, '$10');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'online', 1, '20', 1, '$20');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'online', 1, '30', 2, '$30');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'test', 1, '10', 0, '$10');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'test', 1, '20', 1, '$20');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'promotion_activity', 'test', 1, '30', 2, '$30');


INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'online', 'promotion_activity', 'init',
        '{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Paris\"]}],\"name\":\"Users in Paris\",\"serve\":{\"select\":2}},{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Lille\"]}],\"name\":\"Users in Lille\",\"serve\":{\"select\":1}}],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"7\",\"name\":\"discount 0.7\",\"description\":\"\"},{\"value\":\"8\",\"name\":\"discount 0.8\",\"description\":\"\"},{\"value\":\"9\",\"name\":\"discount 0.9\",\"description\":\"\"}]}',
        0, 1, 0, now(), 1, now(), 1);
INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'test', 'promotion_activity', 'init',
        '{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Paris\"]}],\"name\":\"Users in Paris\",\"serve\":{\"select\":2}},{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Lille\"]}],\"name\":\"Users in Lille\",\"serve\":{\"select\":1}}],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"7\",\"name\":\"discount 0.7\",\"description\":\"\"},{\"value\":\"8\",\"name\":\"discount 0.8\",\"description\":\"\"},{\"value\":\"9\",\"name\":\"discount 0.9\",\"description\":\"\"}]}',
        0, 1, 0, now(), 1, now(), 1);


INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'service_degrade', 'online', 1, 'false', 0, 'close');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'service_degrade', 'online', 1, 'true', 1, 'open');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'service_degrade', 'test', 1, 'false', 0, 'close');
INSERT INTO `variation_history` (`organize_id`, `project_key`, `toggle_key`, `environment_key`, `toggle_version`,
                                 `value`, `value_index`, `name`)
VALUES (1, 'My_First_Project', 'service_degrade', 'test', 1, 'true', 1, 'open');



INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'online', 'service_degrade', 'init',
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"false\",\"name\":\"normal\",\"description\":\"Do time consuming process as usual.\"},{\"value\":\"true\",\"name\":\"degrade\",\"description\":\"Bypass time consuming process.\"}]}',
        0, 1, 0, now(), 1, now(), 1);
INSERT INTO `targeting_version` (`organize_id`, `project_key`, `environment_key`, `toggle_key`, `comment`, `content`,
                                 `disabled`, `version`, `deleted`, `modified_time`, `created_by`, `created_time`,
                                 `modified_by`)
VALUES (1, 'My_First_Project', 'test', 'service_degrade', 'init',
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"false\",\"name\":\"normal\",\"description\":\"Do time consuming process as usual.\"},{\"value\":\"true\",\"name\":\"degrade\",\"description\":\"Bypass time consuming process.\"}]}',
        0, 1, 0, now(), 1, now(), 1);



