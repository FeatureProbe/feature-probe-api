INSERT INTO `attribute`
VALUES (1, 'userId', 'feature_probe', 0, 'system', 'system', now(), now());
INSERT INTO `environment`
VALUES (3, 'online', 'online', 'My_First_Project', 'server-8ed48815ef044428826787e9a238b9c6a479f98c',
        'client-25614c7e03e9cb49c0e96357b797b1e47e7f2dff', 0, now(), 'Admin', now(), 'Admin'),
       (4, 'test', 'test', 'My_First_Project', 'server-3df72071f12a143ba67769ae6837323a2392da6c',
        'client-1b31633671aa8be967697091b72d23da6bf858a7', 0, now(), 'Admin', now(), 'Admin');
INSERT INTO `member`
VALUES (1, 'Admin', '$2a$10$WO5tC7A/nsPe5qmVmjTIPeKD0R/Tm2YsNiVP0geCerT0hIRLBCxZ6', 'ADMIN', 0, now(), now(), 'system',
        now(), 'system');
INSERT INTO `project`
VALUES (2, 'My_First_Project', 'My First Project', '', 0, now(), 'Admin', now(), 'Admin');
INSERT INTO `tag`
VALUES (1, 'feature', 'feature_probe', 0, 'system', 'system', now(), now()),
       (2, 'fix', 'feature_probe', 0, 'system', 'system', now(), now());
INSERT INTO `targeting`
VALUES (21, 'commodity_spike_activity', 'online', 'My_First_Project', 3, 0,
        '{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Paris\"]}],\"name\":\"Users in Paris, the activity price is $30\",\"serve\":{\"select\":2}},{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Lille\"]}],\"name\":\"Users in Lille, the activity price is $20\",\"serve\":{\"select\":1}}],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"7\",\"name\":\"discount 0.7\",\"description\":\"\"},{\"value\":\"8\",\"name\":\"discount 0.8\",\"description\":\"\"},{\"value\":\"9\",\"name\":\"discount 0.9\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (22, 'commodity_spike_activity', 'test', 'My_First_Project', 1, 1,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{},\"variations\":[{\"value\":\"10\",\"name\":\"\",\"description\":\"\"},{\"value\":\"20\",\"name\":\"\",\"description\":\"\"},{\"value\":\"30\",\"name\":\"\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (25, 'color_ab_test', 'online', 'My_First_Project', 3, 0,
        '{\"rules\":[{\"conditions\":[{\"type\":\"string\",\"subject\":\"city\",\"predicate\":\"is one of\",\"objects\":[\"Paris\"]},{\"type\":\"string\",\"subject\":\"gender\",\"predicate\":\"is one of\",\"objects\":[\"famale\"]}],\"name\":\"Paris women show 50% red buttons, 50% blue\",\"serve\":{\"split\":[5000,5000,0]}}],\"disabledServe\":{\"select\":1},\"defaultServe\":{\"select\":1},\"variations\":[{\"value\":\"red\",\"name\":\"The button color is red\",\"description\":\"\"},{\"value\":\"blue\",\"name\":\"The button color is blue\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (26, 'color_ab_test', 'test', 'My_First_Project', 1, 1,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{},\"variations\":[{\"value\":\"red\",\"name\":\"\",\"description\":\"\"},{\"value\":\"blue\",\"name\":\"\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (31, 'product_inventory_fallback', 'online', 'My_first_Project', 2, 0,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"false\",\"name\":\"close\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"open\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (32, 'product_inventory_fallback', 'test', 'My_first_Project', 1, 1,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{},\"variations\":[{\"value\":\"false\",\"name\":\"close\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"open\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (33, 'header_skin', 'online', 'My_First_Project', 2, 0,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"split\":[5000,5000]},\"variations\":[{\"value\":\"false\",\"name\":\"\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now()),
       (34, 'header_skin', 'test', 'My_First_Project', 1, 1,
        '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{},\"variations\":[{\"value\":\"false\",\"name\":\"\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"\",\"description\":\"\"}]}',
        0, 'Admin', 'Admin', now(), now());
INSERT INTO `toggle`
VALUES (11, 'commodity_spike_activity', 'commodity_spike_activity', 'commodity_spike_activity', 'number', 0,
        '[{\"value\":\"10\",\"name\":\"$10\",\"description\":\"\"},{\"value\":\"20\",\"name\":\"$20\",\"description\":\"\"},{\"value\":\"30\",\"name\":\"$30\",\"description\":\"\"}]',
        'My_First_Project', 0, 1, 0, 'Admin', 'Admin', now(), now()),
       (13, 'color_ab_test', 'color_ab_test', 'color_ab_test', 'string', 0,
        '[{\"value\":\"red\",\"name\":\"\",\"description\":\"\"},{\"value\":\"blue\",\"name\":\"\",\"description\":\"\"}]',
        'My_First_Project', 0, 1, 0, 'Admin', 'Admin', now(), now()),
       (16, 'product_inventory_fallback', 'product_inventory_fallback', '', 'boolean', 0,
        '[{\"value\":\"false\",\"name\":\"close\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"open\",\"description\":\"\"}]',
        'My_first_Project', 0, 0, 0, 'Admin', 'Admin', now(), now()),
       (17, 'header_skin', 'header_skin', 'Used to change the skin of page header.', 'boolean', 0,
        '[{\"value\":\"false\",\"name\":\"\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"\",\"description\":\"\"}]',
        'My_First_Project', 0, 1, 0, 'Admin', 'Admin', now(), now());