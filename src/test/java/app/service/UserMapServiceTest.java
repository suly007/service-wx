package app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * 类
 *
 * @author: Huanqd@2018-10-22 14:23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserMapServiceTest {

    @Autowired
    private UserMapService userMapService;

    @Test
    public void test() {
        Map userMap = userMapService.getUserMap();
        log.info("-----:{}", userMap);
        Assert.assertNotNull(userMap);
    }
}