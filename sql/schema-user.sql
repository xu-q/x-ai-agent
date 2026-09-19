-- id 使用 UUID（以 VARCHAR(36) 存储 UUID 字符串，规避 PG JDBC 对 uuid 列的隐式类型转换问题）
CREATE TABLE IF NOT EXISTS sys_user (
    id          VARCHAR(36)   NOT NULL,
    username    VARCHAR(64)   NOT NULL,
    password    VARCHAR(100)  NOT NULL,
    phone       VARCHAR(32),
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER',
    status      SMALLINT      NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted     INT           NOT NULL DEFAULT 0,
    CONSTRAINT sys_user_pkey PRIMARY KEY (id),
    CONSTRAINT sys_user_username_uniq UNIQUE (username)
);
