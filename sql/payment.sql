-- =====================================================================
-- 支付 & 积分充值模块（PostgreSQL）
-- 设计要点：
--   1. pay_order              通用支付单，与具体业务解耦，后续微信等支付可复用同一张表
--   2. points_recharge_order  积分充值业务单，与 pay_order 一对一关联
--   3. 档位配置代码常量写死（PointsPackageConst），不建表
--   4. 入账复用 point_transaction 的 UNIQUE(user_id, biz_no) 做幂等，防重复加积分
-- =====================================================================

-- 通用支付单
CREATE TABLE IF NOT EXISTS pay_order (
    id               VARCHAR(36)  NOT NULL,
    order_no         VARCHAR(64)  NOT NULL,
    user_id          VARCHAR(36)  NOT NULL,
    channel          VARCHAR(16)  NOT NULL,
    biz_type         VARCHAR(32)  NOT NULL,
    biz_no           VARCHAR(64),
    amount           DECIMAL(10,2) NOT NULL,
    subject          VARCHAR(128),
    status           VARCHAR(16)  NOT NULL,
    channel_trade_no VARCHAR(64),
    qr_code          TEXT,
    notify_body      TEXT,
    paid_time        TIMESTAMP,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP,
    deleted          INT          NOT NULL DEFAULT 0,
    CONSTRAINT pay_order_pkey PRIMARY KEY (id),
    CONSTRAINT pay_order_no_uniq UNIQUE (order_no)
);

COMMENT ON TABLE pay_order IS '通用支付单：与具体业务解耦，后续微信/其他支付可复用同一张表';
COMMENT ON COLUMN pay_order.id IS '主键，UUID 字符串';
COMMENT ON COLUMN pay_order.order_no IS '支付单号（业务唯一），同时作为渠道 out_trade_no';
COMMENT ON COLUMN pay_order.user_id IS '下单用户 ID';
COMMENT ON COLUMN pay_order.channel IS '支付渠道：ALIPAY / WECHAT';
COMMENT ON COLUMN pay_order.biz_type IS '业务类型：POINTS_RECHARGE（积分充值）';
COMMENT ON COLUMN pay_order.biz_no IS '关联业务单号，等于 points_recharge_order.order_no';
COMMENT ON COLUMN pay_order.amount IS '支付金额（元）';
COMMENT ON COLUMN pay_order.subject IS '订单标题（支付页展示用）';
COMMENT ON COLUMN pay_order.status IS '支付状态：WAIT_PAY / PAID / CLOSED';
COMMENT ON COLUMN pay_order.channel_trade_no IS '渠道交易流水号（支付宝交易号等）';
COMMENT ON COLUMN pay_order.qr_code IS '预下单返回的二维码原串，待支付时回传前端渲染';
COMMENT ON COLUMN pay_order.notify_body IS '回调原始报文，用于对账';
COMMENT ON COLUMN pay_order.paid_time IS '支付完成时间';
COMMENT ON COLUMN pay_order.create_time IS '创建时间';
COMMENT ON COLUMN pay_order.update_time IS '更新时间';
COMMENT ON COLUMN pay_order.deleted IS '逻辑删除标记：0 未删 1 已删';

-- 积分充值业务单
CREATE TABLE IF NOT EXISTS points_recharge_order (
    id           VARCHAR(36)  NOT NULL,
    order_no     VARCHAR(64)  NOT NULL,
    user_id      VARCHAR(36)  NOT NULL,
    package_key  VARCHAR(32),
    amount       DECIMAL(10,2) NOT NULL,
    points       INT          NOT NULL,
    status       VARCHAR(16)  NOT NULL,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    CONSTRAINT points_recharge_order_pkey PRIMARY KEY (id),
    CONSTRAINT points_recharge_order_no_uniq UNIQUE (order_no)
);

COMMENT ON TABLE points_recharge_order IS '积分充值业务单，与 pay_order 一对一关联，记录本次充值的金额与积分';
COMMENT ON COLUMN points_recharge_order.id IS '主键，UUID 字符串';
COMMENT ON COLUMN points_recharge_order.order_no IS '业务单号（业务唯一），同时作为 pay_order.biz_no';
COMMENT ON COLUMN points_recharge_order.user_id IS '充值用户 ID';
COMMENT ON COLUMN points_recharge_order.package_key IS '档位 key；自定义充值时为 NULL';
COMMENT ON COLUMN points_recharge_order.amount IS '充值金额（元）';
COMMENT ON COLUMN points_recharge_order.points IS '入账积分（自定义充值 = amount*100，下单时锁定，不受后续改档位影响）';
COMMENT ON COLUMN points_recharge_order.status IS '充值状态：WAIT_PAY / PAID / CLOSED（与 pay_order 同步）';
COMMENT ON COLUMN points_recharge_order.create_time IS '创建时间';
COMMENT ON COLUMN points_recharge_order.update_time IS '更新时间';
COMMENT ON COLUMN points_recharge_order.deleted IS '逻辑删除标记：0 未删 1 已删';
