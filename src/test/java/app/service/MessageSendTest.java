package app.service;

import app.pojo.AccessToken;
import app.util.ErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 类
 *
 * @author: Huanqd@2018-10-19 10:47
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MessageSendTest {

    @Autowired
    private QywxMessageService messageSend;

    @Autowired
    private QywxAccessTokenService accessTokenService;

    @Test
    public void sendMessage() {

        AccessToken accessToken =accessTokenService.getSingleAccessToken();
        log.info("accessToken:{}",accessToken);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.setAgentid("1000002");
        message.setMsgtype("text");
        message.setOpenId("XunQingDong1");
        message.setMessage("hi pan");
        message.setResend(true);

        ErrorInfo errorInfo= messageSend.SendMessage(message);
        log.info("ErrorInfo:{}",errorInfo);
    }
}