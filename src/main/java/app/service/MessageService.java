package app.service;

import app.pojo.AccessToken;
import app.pojo.SendResult;
import app.util.Content;
import app.util.Message;
import app.util.MessageUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 类
 *
 * @author: Huanqd@2018-10-19 10:59
 */
@Service
@Slf4j
public class MessageService {

    private AccessTokenService accessTokenService;
    private DataService dataService;
    private RestTemplate restTemplate;
    private UserMapService userMapService;

    @Autowired
    public MessageService(AccessTokenService accessTokenService, DataService dataService, RestTemplate restTemplate, UserMapService userMapService) {
        this.accessTokenService = accessTokenService;
        this.dataService = dataService;
        this.restTemplate = restTemplate;
        this.userMapService = userMapService;
    }

    public SendResult sendMessage(String agentId, String openId, String message, boolean isResend) {
        AccessToken accessToken = accessTokenService.getSingleAccessToken();
        if (accessToken == null) {
            System.out.println("get token failed......");
            dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "get token failed......");
            return new SendResult("801", "get token failed......");
        }
        String token = accessToken.getToken();
        String action = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;
        Message m = new Message();
        m.setTouser(openId);
        m.setMsgtype(MessageUtil.REQ_MESSAGE_TYPE_TEXT);
        m.setText(new Content(message));
        m.setAgentid(agentId);
        log.info("post:{}",JSON.toJSONString(m,true));
        SendResult sendResult = restTemplate.postForObject(action,m,SendResult.class);
        dataService.insertMessage(openId, userMapService.getUserName(openId), message, JSON.toJSONString(sendResult));
        //如果token失效 清空内存中的token,将数据库token设置为过期,消息重发
        if ("40001".equals(sendResult.getErrcode())) {
            accessTokenService.setAccessToken(null);
            dataService.setTokenExpiresd(accessTokenService.getAppid(), accessTokenService.getAppsecret());
            if(isResend){
                SendResult resendInfo=this.sendMessage(agentId,openId,message,false);
                if(!"0".equals(resendInfo.getErrcode())){
                    dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "resend message failed......");
                }
            }
        }
        return sendResult;
    }




}
