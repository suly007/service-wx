package app.controller;

import app.message.corp.PassiveMessage;
import app.service.WxZabbixService;
import app.util.MessageUtil;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 微信请求核心处理类
 *
 * @author: Huanqd@2018-10-17 08:47
 */
@RestController
@Slf4j
public class WxZabbixController {

    @Autowired
    private WxZabbixService wxZabbixService;

    @Resource(name = "zabbixWXBizMsgCrypt")
    private WXBizMsgCrypt wxBizMsgCrypt;

    /**
     * 1.对收到的请求做Urldecode处理
     * 2.通过参数msg_signature对请求进行校验，确认调用者的合法性。
     * 3.解密echostr参数得到消息内容(即msg字段)
     * 4.在1秒内响应GET请求，响应内容为上一步得到的明文消息内容(不能加引号，不能带bom头，不能带换行符)
     *
     * @param msgSignature 企业微信加密签名，msg_signature结合了企业填写的token、请求中的timestamp、nonce参数、加密的消息体
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param echostr      加密的字符串。需要解密得到消息内容明文，解密后有random、msg_len、msg、receiveid四个字段，其中msg即为消息内容明文
     * @return
     */
    @GetMapping("/wxZabbix")
    public String sign(@RequestParam(value = "msg_signature") String msgSignature,
                       @RequestParam(value = "timestamp") String timestamp,
                       @RequestParam(value = "nonce") String nonce,
                       @RequestParam(value = "echostr") String echostr) {
        //需要返回的明文
        String sEchoStr = "";
        try {
            log.info("接收到企业微信zabbix-get请求,msgSignature:{},timestamp:{},nonce:{},echostr:{}", msgSignature, timestamp, nonce, echostr);

            sEchoStr = wxBizMsgCrypt.VerifyURL(msgSignature, timestamp,
                    nonce, echostr);
            log.info("verifyurl echostr: " + sEchoStr);

        } catch (Exception e) {
            log.error("验证失败!", e);
        }

        return sEchoStr;

    }


    @PostMapping("/wxZabbix")
    public String process(@RequestParam(value = "msg_signature") String msgSignature,
                          @RequestParam(value = "timestamp") String timestamp,
                          @RequestParam(value = "nonce") String nonce,
                          HttpServletRequest request) {
        String msg = "";
        try {

            log.info("来自企业微信zabbix-post请求,msgSignature:{},timestamp:{},nonce:{}", msgSignature, timestamp, nonce);

            String reqStr = IOUtils.toString(request.getInputStream(), Charset.forName("utf8"));
            log.info("解密前请求信息: " + reqStr);
            String sMsg = wxBizMsgCrypt.DecryptMsg(msgSignature, timestamp, nonce, reqStr);
            log.info("解密后请求信息: " + sMsg);
            // xml请求解析
            Map<String, String> requestMap = MessageUtil.parseXml(sMsg);
            log.info("字符串解析为map:{}",requestMap);
            PassiveMessage passiveMessage = wxZabbixService.processRequest(requestMap);
            passiveMessage.setMsgSignature(msgSignature);
            passiveMessage.setTimeStamp(timestamp);
            passiveMessage.setNonce(nonce);
            msg = MessageUtil.textMessageToXml(passiveMessage);
            log.info("回复消息加密前:{}",msg);
            msg = wxBizMsgCrypt.EncryptMsg(msg, timestamp, nonce);
            log.info("回复消息加密后:{}",msg);

        } catch (Exception e) {
            log.error("处理企业微信post请求失败",e);
        }
        return msg;
    }


}
