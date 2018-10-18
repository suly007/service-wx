package app.task;

import app.pojo.Stocks;
import app.service.DataService;
import app.service.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 10:18
 */
@Component
@Slf4j
@Profile({"test"})
public class MonitorTask {

    @Autowired
    private Monitor monitor;

    @Autowired
    private DataService dataService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH.mm");

    private final int fixedDelay = 200;


    @Scheduled(fixedDelay = fixedDelay)
    private void monitor() {

        if (isOpen()) {
            Map<String, Stocks> currentInfo = monitor.getAllCurrentInfo();
            //chg process
            monitor.processChg(currentInfo);
            //comp process
            monitor.processComp(currentInfo);
            monitor.reLoadList();
            //delBlack();
            log.info(" ok.....");
        } else {
            try {
                dataService.initData();
                dataService.delData();
                // 休眠10分钟
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e) {
                log.error("中断异常", e);
            }

        }
    }


    /**
     * 是否开盘时间
     *
     * @return
     */
    private boolean isOpen() {

        double amBegin = 9.15;
        double amEnd = 11.30;
        double pmBegin = 13.00;
        double pmEnd = 15.00;

        double now = Double.valueOf(simpleDateFormat.format(new Date()));

        if (now >= amBegin && now <= amEnd) {
            return true;
        }
        if (now > pmBegin && now < pmEnd) {
            return true;
        }

        return false;
    }
}
