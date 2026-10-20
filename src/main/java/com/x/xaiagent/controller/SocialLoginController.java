package com.x.xaiagent.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.x.xaiagent.comment.R;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.service.AlipayLoginService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * 三方登录（当前仅支付宝），内嵌二维码扫码模式：
 * - GET /login/alipay/qr      生成 state（Redis 存 5 分钟）+ 授权地址 + 二维码 PNG data URL
 * - GET /login/alipay/status  PC 端轮询：手机确认后此处建立 PC 会话并返回用户信息
 * - GET /login/alipay/callback 支付宝回调（发生在手机端）：登录/建号后把 state 标记为成功
 * 为什么用 Redis 存 state：二维码被手机扫后，授权确认与回调都发生在手机浏览器上，
 * 与 PC 不是同一个 HttpSession，必须用跨端共享的 Redis 传递“已确认”信号。
 */
@RestController
@RequestMapping("/login")
public class SocialLoginController {

    @Resource
    private AlipayLoginService alipayLoginService;

    @Resource
    private StringRedisTemplate redis;

    /** state 在 Redis 中的 key 前缀 */
    private static final String STATE_KEY = "login:alipay:state:";
    /** state 有效期：二维码超时时间 */
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    /** 生成扫码登录二维码：state 存 Redis，授权地址渲染成 PNG data URL（前端无需引二维码库） */
    @GetMapping("/alipay/qr")
    public R<Map<String, Object>> alipayQr() {
        String state = UUID.randomUUID().toString();
        redis.opsForValue().set(STATE_KEY + state, "PENDING", STATE_TTL);
        String authorizeUrl = alipayLoginService.buildAuthorizeUrl(state);
        return R.ok(Map.of(
                "state", state,
                "authorizeUrl", authorizeUrl,
                "qrImage", toQrDataUrl(authorizeUrl)
        ));
    }

    /** PC 端轮询扫码状态：PENDING / EXPIRED / SUCCESS（成功时建立 PC 会话并返回用户信息） */
    @GetMapping("/alipay/status")
    public R<Map<String, Object>> alipayStatus(@RequestParam String state, HttpServletRequest request) {
        String value = redis.opsForValue().get(STATE_KEY + state);
        if (value == null) {
            return R.ok(Map.of("status", "EXPIRED"));
        }
        if (value.startsWith("OK:")) {
            User user = alipayLoginService.findById(value.substring("OK:".length()));
            redis.delete(STATE_KEY + state);
            if (user == null) {
                return R.ok(Map.of("status", "EXPIRED"));
            }
            // 登录态写入 PC 浏览器的会话（与 UserController.login 一致）
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            return R.ok(Map.of("status", "SUCCESS", "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
            )));
        }
        return R.ok(Map.of("status", "PENDING"));
    }

    /** 支付宝回调（手机端）：登录/建号后把 state 标记为成功，手机上展示确认完成页 */
    @GetMapping("/alipay/callback")
    public void alipayCallback(@RequestParam("auth_code") String authCode,
                               @RequestParam(value = "state", required = false) String state,
                               HttpServletResponse response) throws IOException {
        User user = alipayLoginService.handleCallback(authCode);
        if (state != null) {
            redis.opsForValue().set(STATE_KEY + state, "OK:" + user.getId(), STATE_TTL);
        }
        writeConfirmPage(response, user);
    }

    /** 兼容保留：返回授权地址（老流程/调试用），正式内嵌二维码走 /alipay/qr */
    @GetMapping("/alipay/url")
    public R<String> alipayUrl(HttpServletRequest request) {
        String state = UUID.randomUUID().toString();
        request.getSession(true).setAttribute("alipay_oauth_redirect_" + state, "/");
        return R.ok(alipayLoginService.buildAuthorizeUrl(state));
    }

    /** 授权地址 → PNG 二维码 data URL（zxing，前端 img 直接展示） */
    private String toQrDataUrl(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 260, 260);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", os);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("二维码生成失败：" + e.getMessage());
        }
    }

    /** 手机端确认完成页：提示回到电脑查看 */
    private void writeConfirmPage(HttpServletResponse response, User user) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String username = user.getUsername() == null ? ""
                : user.getUsername().replace("<", "&lt;").replace(">", "&gt;");
        String html = "<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>授权成功</title><style>"
                + "body{font-family:system-ui,-apple-system,sans-serif;display:flex;align-items:center;justify-content:center;"
                + "height:100vh;margin:0;background:#f5f7fa}"
                + ".card{background:#fff;padding:36px 40px;border-radius:16px;box-shadow:0 8px 30px rgba(0,0,0,.08);text-align:center}"
                + ".ok{color:#00b42a;font-size:52px;line-height:1}.t{margin:16px 0 8px;font-size:20px;color:#1d2129}"
                + "p{color:#86909c;margin:6px 0}</style></head><body><div class=\"card\"><div class=\"ok\">✓</div>"
                + "<div class=\"t\">授权成功</div><p>账号：" + username + "</p>"
                + "<p>请回到电脑页面继续，本页可关闭</p></div></body></html>";
        response.getWriter().write(html);
    }
}
