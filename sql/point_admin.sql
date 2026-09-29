-- =====================================================================
-- 积分管理模块（PostgreSQL）
-- 设计要点：
--   1. point_config 积分规则配置：单行表（恒一行），UUID 主键；
--      规则值二期接入签到逻辑，本期仅存取
--   2. point_transaction 增加审计列 operator_id：
--      后台调整积分时记录操作管理员，普通业务流水为 NULL
-- =====================================================================

-- 积分规则配置（单行表）
CREATE TABLE IF NOT EXISTS point_config (
    id                VARCHAR(36) NOT NULL,               -- 主键，UUID 字符串，单行表
    sign_base_points  INT         NOT NULL DEFAULT 5,     -- 签到基础分
    continuous_bonus  INT         NOT NULL DEFAULT 1,     -- 连续签到加成（二期接入）
    operator_id       VARCHAR(36),                        -- 最后操作人（sys_user.id）
    update_time       TIMESTAMP,                          -- 最后修改时间
    CONSTRAINT point_config_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE point_config IS '积分规则配置（单行表）';
COMMENT ON COLUMN point_config.id IS '主键，UUID 字符串，单行表';
COMMENT ON COLUMN point_config.sign_base_points IS '签到基础分，每日首次签到固定获得（二期接入签到逻辑）';
COMMENT ON COLUMN point_config.continuous_bonus IS '连续签到加成积分（二期接入签到逻辑，本期仅存取）';
COMMENT ON COLUMN point_config.operator_id IS '最后操作人用户 ID（sys_user.id，保存规则的管理员）';
COMMENT ON COLUMN point_config.update_time IS '最后修改时间';

-- 积分流水增加操作人审计列（可空：普通业务流水为 NULL）
ALTER TABLE point_transaction ADD COLUMN IF NOT EXISTS operator_id VARCHAR(36);
COMMENT ON COLUMN point_transaction.operator_id IS '操作人用户 ID（后台调整积分时记录管理员 sys_user.id，普通业务流水为 NULL）';

-- 初始化默认规则行（幂等：无任何行时插入默认值）
INSERT INTO point_config (id, sign_base_points, continuous_bonus)
SELECT '00000000-0000-0000-0000-000000000001', 5, 1
WHERE NOT EXISTS (SELECT 1 FROM point_config);
