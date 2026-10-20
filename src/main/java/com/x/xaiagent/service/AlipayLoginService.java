package com.x.xaiagent.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.xaiagent.config.AlipayProperties;
import com.x.xaiagent.constant.LoginSource;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付宝扫码登录（OAuth2）：
 * - buildAuthorizeUrl：拼接网页授权地址，PC 打开即显示扫码二维码
 * - handleCallback：用 auth_code 换 token + 用户信息，按 open_id 找/建本地用户
 * 复用支付宝沙箱网关与密钥（与支付共用 AlipayProperties）。
 */
@Service
public class AlipayLoginService {

    @Resource
    private AlipayProperties props;

    @Resource
    private UserMapper userMapper;

    private AlipayClient client;

    @PostConstruct
    public void init() {
        client = new DefaultAlipayClient(
                props.getGateway(), props.getAppId(), props.getPrivateKey(),
                props.getFormat(), props.getCharset(), props.getAlipayPublicKey(), props.getSignType());
    }

    /** 拼接支付宝网页授权地址（PC 打开即显示扫码登录二维码） */
    public String buildAuthorizeUrl(String state) {
        String host = props.getGateway().contains("alipaydev.com")
                ? "https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm"
                : "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm";
        return host
                + "?app_id=" + props.getAppId()
                + "&scope=auth_user"
                + "&redirect_uri=" + urlEncode(props.getOauthRedirectUrl())
                + "&state=" + state;
    }

    /** 处理 OAuth 回调：用 auth_code 换 token 与用户信息，找/建本地用户后返回 */
    public User handleCallback(String authCode) {
        try {
            AlipaySystemOauthTokenRequest tokenReq = new AlipaySystemOauthTokenRequest();
            tokenReq.setGrantType("authorization_code");
            tokenReq.setCode(authCode);
            AlipaySystemOauthTokenResponse tokenResp = client.execute(tokenReq);
            if (!tokenResp.isSuccess()) {
                throw new BusinessException("支付宝授权失败：" + tokenResp.getSubMsg());
            }
            String alipayUserId = tokenResp.getUserId();
            String accessToken = tokenResp.getAccessToken();

            String nickname = null;
            String avatar = null;
            try {
                AlipayUserInfoShareResponse infoResp = client.execute(new AlipayUserInfoShareRequest(), accessToken);
                if (infoResp.isSuccess()) {
                    nickname = infoResp.getNickName();
                    avatar = infoResp.getAvatar();
                }
            } catch (AlipayApiException ignored) {
                // 昵称/头像获取失败不阻塞登录，使用兜底
            }

            return findOrCreate(alipayUserId, nickname, avatar);
        } catch (AlipayApiException e) {
            throw new BusinessException("支付宝登录异常：" + e.getMessage());
        }
    }

    /** 按主键查用户（扫码轮询建立 PC 会话时使用） */
    public User findById(String id) {
        return userMapper.selectById(id);
    }

    private User findOrCreate(String alipayUserId, String nickname, String avatar) {
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getSource, LoginSource.ALIPAY.getCode())
                .eq(User::getOpenId, alipayUserId));
        if (exist != null) {
            return exist;
        }
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setUsername("alipay_" + alipayUserId);
        // 三方登录用户无密码，写入随机 BCrypt 哈希，避免被密码登录命中
        u.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt()));
        u.setAvatar(avatar);
        u.setRole(RoleConstants.USER);
        u.setStatus(1);
        u.setSource(LoginSource.ALIPAY.getCode());
        u.setOpenId(alipayUserId);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        u.setDeleted(0);
        userMapper.insert(u);
        return u;
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
