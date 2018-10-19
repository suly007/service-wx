package app.service;

import app.pojo.AccessToken;
import app.util.Content;
import app.util.ErrorInfo;
import app.util.Message;
import app.util.UserMapUtil;
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
public class QywxMessageService implements MessageService {

    @Autowired
    private QywxAccessTokenService accessTokenService;
    @Autowired
    private DataService dataService;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ErrorInfo SendMessage(app.service.Message msg) {

        String agentid = msg.getAgentid();
        String message = msg.getMessage();
        String msgtype = msg.getMsgtype();
        String openId = msg.getOpenId();
        boolean resend = msg.isResend();
        AccessToken accessToken = accessTokenService.getSingleAccessToken();
        if (accessToken == null) {
            System.out.println("get token failed......");
            dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "get token failed......");
            return new ErrorInfo("801", "get token failed......");
        }
        String token = accessToken.getToken();
        String action = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + token;
        Message m = new Message();
        m.setTouser(openId);
        m.setMsgtype(msgtype);
        m.setText(new Content(message));
        m.setAgentid(agentid);
        log.info("post:{}",JSON.toJSONString(m,true));
        ErrorInfo errorInfo = restTemplate.postForObject(action,m,ErrorInfo.class);
        dataService.insertMessage(openId, UserMapUtil.userMap.get(openId), message, JSON.toJSONString(errorInfo));
        //如果token失效 清空内存中的token,将数据库token设置为过期,消息重发
        if ("40001".equals(errorInfo.getErrcode())) {
            accessTokenService.setAccessToken(null);
            dataService.setTokenExpiresd(accessTokenService.getAppid(), accessTokenService.getAppsecret());
            if(resend){
                msg.setResend(false);
                ErrorInfo resendInfo=this.SendMessage(msg);
                if(!"0".equals(resendInfo.getErrcode())){
                    dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "resend message failed......");
                }
            }
        }
        return errorInfo;
    }


}
