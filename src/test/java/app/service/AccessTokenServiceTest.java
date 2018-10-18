package app.service;

import app.pojo.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * AccessTokenService测试类
 *
 * @author: Huanqd@2018-10-17 17:08
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AccessTokenServiceTest {

    @Autowired
    private AccessTokenService accessTokenService;


    @Before
    public void setUp() throws Exception {
      log.info("setUp do nothing");
    }

    @After
    public void tearDown() throws Exception {
        log.info("tearDown do nothing");
    }

    @Test
    public void getSingleAccessToken() {
        AccessToken accessToken=accessTokenService.getSingleAccessToken();
        log.info("accessToken:{}",accessToken);
        assertNotNull(accessToken);
        assertNotNull(accessToken.getToken());
    }

    @Test
    public void getAccessToken() {

    }

    @Test
    public void setAccessToken() {
    }
}