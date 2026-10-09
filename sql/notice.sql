-- =====================================================================
-- 消息发布 / 系统通知模块（PostgreSQL）
-- 设计要点：
--   1. notice        公告主表：管理员起草 → 发布 → 撤回，逻辑删除
--   2. notice_read   已读表：(user_id, notice_id) 唯一，记录某用户已读某公告
--      read 状态 = notice_read 里是否存在对应记录，天然幂等
--   3. scope 可见角色集合：逗号分隔（如 "USER,GUEST"），
--      可选 ADMIN / USER / GUEST 任意组合，登录用户角色命中即可见
-- =====================================================================

-- 公告主表
CREATE TABLE IF NOT EXISTS notice (
    id           VARCHAR(36)  NOT NULL,
    type         VARCHAR(16)  NOT NULL,               -- SYSTEM / ACTIVITY / UPDATE
    title        VARCHAR(100) NOT NULL,               -- 标题
    content      TEXT         NOT NULL,               -- 正文
    scope        VARCHAR(64)  NOT NULL,               -- 可见角色集合，逗号分隔，如 USER,GUEST
    status       VARCHAR(16)  NOT NULL,               -- DRAFT / PUBLISHED / WITHDRAWN
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP,
    publish_time TIMESTAMP,                           -- 首次发布时间
    operator_id  VARCHAR(36),                         -- 最后操作人（sys_user.id）
    deleted      SMALLINT     NOT NULL DEFAULT 0,     -- 逻辑删除：0 未删，1 已删
    CONSTRAINT notice_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  notice                IS '公告/系统通知主表';
COMMENT ON COLUMN notice.id             IS '主键，UUID 字符串（Java 端生成）';
COMMENT ON COLUMN notice.type           IS '通知类型：SYSTEM 系统 / ACTIVITY 活动 / UPDATE 更新';
COMMENT ON COLUMN notice.title          IS '标题';
COMMENT ON COLUMN notice.content        IS '正文内容';
COMMENT ON COLUMN notice.scope          IS '可见角色集合，逗号分隔，取值 ADMIN / USER / GUEST 任意组合，登录用户角色命中即可见';
COMMENT ON COLUMN notice.status         IS '状态：DRAFT 草稿 / PUBLISHED 已发布 / WITHDRAWN 已撤回';
COMMENT ON COLUMN notice.create_time    IS '创建时间';
COMMENT ON COLUMN notice.update_time    IS '最后修改时间';
COMMENT ON COLUMN notice.publish_time   IS '首次发布时间（重复发布不覆盖）';
COMMENT ON COLUMN notice.operator_id    IS '最后操作人用户 ID（sys_user.id）';
COMMENT ON COLUMN notice.deleted        IS '逻辑删除：0 未删，1 已删';

-- 已读表
CREATE TABLE IF NOT EXISTS notice_read (
    id          VARCHAR(36) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    notice_id   VARCHAR(36) NOT NULL,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notice_read_pkey PRIMARY KEY (id),
    CONSTRAINT notice_read_uniq UNIQUE (user_id, notice_id)
);

COMMENT ON TABLE  notice_read            IS '通知已读记录';
COMMENT ON COLUMN notice_read.id         IS '主键，UUID 字符串（Java 端生成）';
COMMENT ON COLUMN notice_read.user_id    IS '用户 ID（sys_user.id）';
COMMENT ON COLUMN notice_read.notice_id  IS '公告 ID（notice.id）';
COMMENT ON COLUMN notice_read.create_time IS '已读时间';
