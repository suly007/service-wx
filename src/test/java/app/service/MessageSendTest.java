package app.service;

import app.pojo.AccessToken;
import app.pojo.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
    private MessageService messageService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Test
    public void sendMessage() {

        AccessToken accessToken =accessTokenService.getSingleAccessToken();
        log.info("accessToken:{}",accessToken);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SendResult sendResult= messageService.sendMessage("1000002","XunQingDong","你好" ,true);
        log.info("sendResult:{}",sendResult);
        Assert.assertEquals(sendResult.getErrmsg(),"ok");
        Assert.assertEquals(sendResult.getInvaliduser(),"");
    }
}