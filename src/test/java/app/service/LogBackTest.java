package app.service;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 08:54
 */
public class LogBackTest {


    @Test
    public void test(){
        Logger logger = LoggerFactory.getLogger(getClass());



        String message = String.format("你好:%s",1);
        logger.info(message);
    }


}
