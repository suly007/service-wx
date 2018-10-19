package app.service;

import app.pojo.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 获取access_token服务类
 *
 * @author: Huanqd@2018-10-18 9:42
 */
@Service
@Slf4j
public class QywxAccessTokenService{

    private AccessToken accessToken = new AccessToken();
    @Value("${qywx.corpId}")
    private String appid;
    @Value("${qywx.zabbix.encodingAESKey}")
    private String appsecret;

    // https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRECT
    private final static String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    private DataService dataService;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 获取access_token
     *
     * @return
     */
    public AccessToken getSingleAccessToken() {
        if (isTokenExpires(accessToken)) {
            Map<String, Object> token = dataService.getToken(appid, appsecret);
            if (token.isEmpty()) {
                getNewToken();
            } else {
                try {
                    accessToken.setToken(MapUtils.getString(token, "token"));
                    accessToken.setExpiresIn(MapUtils.getInteger(token, "expiresin"));
                    accessToken.setExpiresDate((Date) MapUtils.getObject(token, "expiresdate"));
                    log.info("get token by db......");
                } catch (Exception e) {
                    getNewToken();
                }
            }
        } else {
            log.info("get token by memory......");
        }
        return accessToken;
    }

    private boolean isTokenExpires(AccessToken accessToken) {
        boolean flag = false;
        Date expiresDate = accessToken.getExpiresDate();
        // 如果此 Timestamp 对象与给定对象相等，则返回值 0；如果此 Timestamp 对象早于给定参数，则返回小于 0 的值；如果此
        // Timestamp 对象晚于给定参数，则返回大于 0 的值。
        if (expiresDate == null || new Date().compareTo(expiresDate) >= 0) {
            flag = true;
        }
        return flag;
    }

    private void getNewToken() {
        Date now = new Date();
        String requestUrl = String.format(ACCESS_TOKEN_URL, appid, appsecret);

        Map map =restTemplate.getForObject(requestUrl,Map.class);

        if(MapUtils.isNotEmpty(map)){
            log.debug("获取token数据:{}",map);
            try {
                log.info("get token by http......");
                accessToken = new AccessToken();
                String token = MapUtils.getString(map,"access_token");
                accessToken.setToken(token);
                int expiresIn = MapUtils.getInteger(map,"expires_in");
                accessToken.setExpiresIn(expiresIn);
                now.setTime(now.getTime() + expiresIn * 1000);
                accessToken.setExpiresDate(now);
                // 将token存入数据库
                dataService.insertToken(appid, appsecret, token, String.valueOf(expiresIn), simpleDateFormat.format(now));
            } catch (Exception e) {
                accessToken = null;
                // 获取token失败
                log.error("存储token异常!",e);
            }
        }
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public String getAppid() {
        return appid;
    }


    public String getAppsecret() {
        return appsecret;
    }

}