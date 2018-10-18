package app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 服务启动后,启动monitor线程
 *
 * @author: Huanqd@2018-10-18 9:53
 */
@Component
@Slf4j
public class AfterServiceStarted implements ApplicationRunner {

    @Autowired
    private Monitor monitor;

    /**
     * 会在服务启动完成后立即执行
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //monitor.start();
        log.info("Successful service startup!");
    }
}
