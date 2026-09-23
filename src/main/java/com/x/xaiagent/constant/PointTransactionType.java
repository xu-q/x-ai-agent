package com.x.xaiagent.constant;

/**
 * 积分流水业务类型（对齐前端枚举）。
 * 入库存 name()（如 SIGN_IN），JSON 序列化也是 name()，与前端 r.type 判断一致。
 */
public enum PointTransactionType {

    /** 每日签到 */
    SIGN_IN("签到"),

    /** 充值 / 兑换获得 */
    RECHARGE("充值"),

    /** 消费扣减 */
    SPEND("消费"),

    /** 后台人工调整（可正可负） */
    ADMIN_ADJUST("后台调整");

    private final String label;

    PointTransactionType(String label) {
        this.label = label;
    }

    /** 中文展示名（仅用于日志/管理端，前端 type 字段传的是 name()） */
    public String getLabel() {
        return label;
    }
}
