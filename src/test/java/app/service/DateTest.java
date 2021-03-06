package app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 10:42
 */
@Slf4j
public class DateTest {

    @Test
    public void test() {

        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        try {
            log.info(sdf.parse("08.00").toString());

            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.DATE, 1);
            log.info("{}", ca.get(Calendar.DAY_OF_WEEK));
            log.info(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}
