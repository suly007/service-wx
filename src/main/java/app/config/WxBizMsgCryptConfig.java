package app.config;

import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 企业微信解密类
 *
 * @author: Huanqd@2018-10-18 15:18
 */
@Configuration
@Slf4j
public class WxBizMsgCryptConfig {

    @Value("${weixin.zabbix.corpID}")
    private String zabbixCorpID;

    @Value("${weixin.zabbix.token}")
    private String zabbixToken;

    @Value("${weixin.zabbix.encodingAESKey}")
    private String zabbixEncodingAESKey;

    @Bean("zabbixWXBizMsgCrypt")
    public WXBizMsgCrypt getZabbixWXBizMsgCrypt() {
        try {
            return new WXBizMsgCrypt(zabbixToken, zabbixEncodingAESKey, zabbixCorpID);
        } catch (AesException e) {
            log.error("构造zabbixWXBizMsgCrypt失败", e);
            return null;
        }
    }

}
