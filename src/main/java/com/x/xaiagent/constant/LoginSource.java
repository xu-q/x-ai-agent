package com.x.xaiagent.constant;

/**
 * 用户注册/登录来源，对应 sys_user.source 字段的取值。
 */
public enum LoginSource {
    USERNAME("username"),
    PHONE("phone"),
    ALIPAY("alipay"),
    WECHAT("wechat"),
    QQ("qq");

    private final String code;

    LoginSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /** 由字符串解析，未知/空则归为用户名登录 */
    public static LoginSource of(String code) {
        if (code == null) {
            return USERNAME;
        }
        for (LoginSource s : values()) {
            if (s.code.equalsIgnoreCase(code)) {
                return s;
            }
        }
        return USERNAME;
    }
}
