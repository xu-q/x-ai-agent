-- id 使用 UUID（以 VARCHAR(36) 存储 UUID 字符串，规避 PG JDBC 对 uuid 列的隐式类型转换问题）
CREATE TABLE IF NOT EXISTS sys_user (
    id          VARCHAR(36)   NOT NULL,
    username    VARCHAR(64)   NOT NULL,
    password    VARCHAR(100)  NOT NULL,
    phone       VARCHAR(32),
    avatar      VARCHAR(512),
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER',
    status      SMALLINT      NOT NULL DEFAULT 1,
    source      VARCHAR(32)   NOT NULL DEFAULT 'username',
    open_id     VARCHAR(128),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted     INT           NOT NULL DEFAULT 0,
    CONSTRAINT sys_user_pkey PRIMARY KEY (id),
    CONSTRAINT sys_user_username_uniq UNIQUE (username)
);

COMMENT ON TABLE sys_user IS '系统用户表';

COMMENT ON COLUMN sys_user.id IS '用户主键（UUID 字符串）';
COMMENT ON COLUMN sys_user.username IS '登录用户名，唯一';
COMMENT ON COLUMN sys_user.password IS 'BCrypt 加密后的密码（三方登录用户写入随机哈希，无明文密码）';
COMMENT ON COLUMN sys_user.phone IS '手机号（可选）';
COMMENT ON COLUMN sys_user.avatar IS '头像 URL（三方登录时从平台同步）';
COMMENT ON COLUMN sys_user.role IS '角色：ADMIN / USER / GUEST';
COMMENT ON COLUMN sys_user.status IS '状态：1 启用，0 禁用';
COMMENT ON COLUMN sys_user.source IS '注册/登录来源：username / phone / alipay / wechat / qq（见 LoginSource 枚举）';
COMMENT ON COLUMN sys_user.open_id IS '第三方平台用户唯一标识（支付宝 user_id、微信 openid 等），非三方登录为 NULL';
COMMENT ON COLUMN sys_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_user.update_time IS '更新时间';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除：0 未删除，1 已删除';
