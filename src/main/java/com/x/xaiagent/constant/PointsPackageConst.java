package com.x.xaiagent.constant;

import java.math.BigDecimal;
import java.util.List;

/**
 * 积分充值档位配置（代码常量写死，不建表）。
 * 自定义金额按 1 元 = 100 积分计算，无赠送。
 */
public final class PointsPackageConst {

    private PointsPackageConst() {
    }

    /**
     * 单个档位定义。
     *
     * @param key          档位 key（下单时传此值）
     * @param title        档位名称
     * @param price        售价（元）
     * @param totalPoints  到账总积分
     * @param bonusPoints  赠送积分（仅展示用，totalPoints 已含赠送）
     * @param tag          标签文案
     * @param desc         副标题/说明（如“送 600”）
     * @param hot          是否热销
     * @param sort         展示排序（升序）
     */
    public record Def(String key, String title, BigDecimal price, int totalPoints,
                      int bonusPoints, String tag, String desc, boolean hot, int sort) {
    }

    public static final List<Def> LIST = List.of(
            new Def("P6", "首充特惠", new BigDecimal("6"), 1200, 600, "首充奖励", "送600", false, 1),
            new Def("P18", "人气特惠", new BigDecimal("18"), 2400, 600, "人气特惠", "送600", false, 2),
            new Def("P45", "热销爆款", new BigDecimal("45"), 6500, 2000, "热销爆款", "送2000", true, 3),
            new Def("P88", "尊享回馈", new BigDecimal("88"), 14000, 5200, "尊享回馈", "送5200", false, 4),
            new Def("P128", "至尊旗舰", new BigDecimal("128"), 22000, 9200, "至尊旗舰", "送9200", false, 5)
    );

    /**
     * 按 key 取档位，不存在返回 null。
     */
    public static Def get(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return LIST.stream().filter(d -> d.key().equals(key)).findFirst().orElse(null);
    }

    /**
     * 自定义金额换算积分：1 元 = 100 积分，无赠送。
     */
    public static int customAmountToPoints(java.math.BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }
}
