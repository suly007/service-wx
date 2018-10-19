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

    // 输出ok间隔次数
    private int logOkInterval = 100;

    // 计数器
    private int times = 0;


    @Scheduled(fixedDelay = fixedDelay)
    private void monitor() {
        times++;
        if (isOpen()) {
            Map<String, Stocks> currentInfo = monitor.getAllCurrentInfo();
            //chg process
            monitor.processChg(currentInfo);
            //comp process
            monitor.processComp(currentInfo);
            monitor.reLoadList();
            //delBlack();
            if (times % logOkInterval == 0) {
                log.info(" ok.....");
            }
        } else {
            try {
                dataService.initData();
                dataService.delData();
                times = 0;
                log.info("休眠10分钟");
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

        double begin = 9.0;
        double end = 16.0;

        double now = Double.valueOf(simpleDateFormat.format(new Date()));

        if (now > begin && now < end) {
            return true;
        }

        return false;
    }
}
