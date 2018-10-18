package app.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 10:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataServiceTest {

    @Autowired
    private DataService dataService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getStockMapListByAppidChg() {
    }

    @Test
    public void getStockMapListByAppidComp() {
    }

    @Test
    public void getStockListStr() {
    }

    @Test
    public void getStockListStrByOpenID() {
    }

    @Test
    public void updateDiffWarnTime() {
    }

    @Test
    public void chgBaseDiffByUser() {
    }

    @Test
    public void updateChange() {
    }

    @Test
    public void dataInit() {

        int initNum =dataService.initData();
        assertTrue(initNum>0);

    }

    @Test
    public void notExist() {
    }

    @Test
    public void delExist() {
    }

    @Test
    public void delAllExist() {
    }

    @Test
    public void insertData() {
    }

    @Test
    public void updateBaseDiff() {
    }

    @Test
    public void delData() {
    }

    @Test
    public void getToken() {
    }

    @Test
    public void setTokenExpiresd() {
    }

    @Test
    public void insertToken() {
    }

    @Test
    public void insertMessage() {
    }

    @Test
    public void insertErrorInfo() {
    }
}