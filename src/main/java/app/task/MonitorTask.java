package app.task;

import app.pojo.Stocks;
import app.service.DataService;
import app.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 10:18
 */
@Component
@Slf4j
@Profile({"prod"})
public class MonitorTask {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private DataService dataService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH.mm");

    private final int fixedDelay = 200;

    // 输出ok间隔次数
    private int logOkInterval = 10;

    // 计数器
    private int times = 0;


    @Scheduled(fixedDelay = fixedDelay)
    private void monitor() {
        times++;
        if (isOpen()) {
            Map<String, Stocks> currentInfo = monitorService.getAllCurrentInfo();
            //chg process
            monitorService.processChg(currentInfo);
            //comp process
            monitorService.processComp(currentInfo);
            monitorService.reLoadList();
            //delBlack();
            if (times % logOkInterval == 0) {
                log.info(" ok.....");
            }
        } else {
            try {
                dataService.initData();
                dataService.delData();
                times = 0;
                log.info("休眠15分钟");
                // 休眠10分钟
                Thread.sleep(1500 * 60 * 10);

            } catch (InterruptedException e) {
                log.error("中断异常", e);
            }

        }
    }


    /**
     * 是否开盘时间
     */
    private boolean isOpen() {

        double begin = 9.0;
        double end = 15.0;

        double now = Double.valueOf(simpleDateFormat.format(new Date()));
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        // 时间介于9-15并且非周六和周日
        return now > begin && now < end && day != 7 && day != 1;

    }
}
