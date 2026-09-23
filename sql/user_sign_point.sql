-- =====================================================================
-- 签到 & 积分模块（PostgreSQL）
-- 设计要点：
--   1. user_sign_month  位图表：一人一月一行，31 天装进一个 INT（12 行/人/年）
--      第 i 位（bit0 起）= 当月第 i+1 天是否签到；UNIQUE 主键天然防重
--   2. point_transaction 积分流水：append-only 账本，只增不改；
--      UNIQUE (user_id, biz_no) 保证同一业务（如某天签到）终身只记一次
--   3. point_account     积分余额：查询快；与流水同库同事务，可 sum 流水重算对账
-- =====================================================================

-- 签到位图
CREATE TABLE IF NOT EXISTS user_sign_month (
    user_id     VARCHAR(36) NOT NULL,
    sign_month  CHAR(7)     NOT NULL,               -- 'yyyy-MM'
    sign_bits   INT         NOT NULL DEFAULT 0,     -- 位图：第 i 位 = 当月第 i+1 天
    update_time TIMESTAMP,
    CONSTRAINT user_sign_month_pkey PRIMARY KEY (user_id, sign_month)
);

-- 积分流水（只增不改）
CREATE TABLE IF NOT EXISTS point_transaction (
    id          VARCHAR(36)  NOT NULL,
    user_id     VARCHAR(36)  NOT NULL,
    points      INT          NOT NULL,              -- 正=获得 负=消耗
    type        VARCHAR(32)  NOT NULL,              -- SIGN_IN / RECHARGE / SPEND / ADMIN_ADJUST
    biz_no      VARCHAR(64)  NOT NULL,              -- 业务幂等号，如 SIGN:2026-09-23
    remark      VARCHAR(255),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT point_transaction_pkey PRIMARY KEY (id),
    CONSTRAINT point_transaction_biz_uniq UNIQUE (user_id, biz_no)
);

-- 积分余额
CREATE TABLE IF NOT EXISTS point_account (
    user_id     VARCHAR(36) NOT NULL,
    balance     INT         NOT NULL DEFAULT 0,
    update_time TIMESTAMP,
    CONSTRAINT point_account_pkey PRIMARY KEY (user_id)
);
