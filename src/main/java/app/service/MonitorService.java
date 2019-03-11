package app.service;

import app.pojo.Stocks;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Huanqd@2018-10-19 14:41
 */
@Service
@Slf4j
public class MonitorService {

    /**
     * 企业号Id
     */
    private String corpId;

    /**
     * 程序agentId
     */
    private String agentId;


    private DataService dataService;
    private List<Map<String, Object>> stockMapListChg;
    private List<Map<String, Object>> stockMapListComp;
    private List<Map<String, Object>> blackMapList;
    private String stockListStr;

    private MessageService messageService;

    private SimpleDateFormat simpleDateFormat;
    private static final java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");


    @Autowired
    public MonitorService(@Value("${qywx.corpId}") String corpId, @Value("${qywx.zabbix.agentId}") String agentId,
                          MessageService messageService, DataService dataService, SimpleDateFormat simpleDateFormat) {
        this.messageService = messageService;
        this.dataService = dataService;
        this.simpleDateFormat = simpleDateFormat;
        this.corpId = corpId;
        this.agentId = agentId;
        stockMapListChg = dataService.getStockMapListByCorpIdChg(corpId);
        stockMapListComp = dataService.getStockMapListByCorpIdComp(corpId);
        blackMapList = dataService.getBlackList();
        stockListStr = dataService.getStockListStr();
    }


    public Map<String, Stocks> getAllCurrentInfo() {
        String url = "http://hq.sinajs.cn/rn=" +
                RandomUtils.nextLong() +
                "&list=" +
                stockListStr;
        return getCurrentInfo(url);
    }

    Map<String, Stocks> getCurrentInfo(String strUrl) {
        Map<String, Stocks> resultMap = new HashMap<String, Stocks>();
        StringBuilder contentBuf = new StringBuilder();
        try {
            URL url = new URL(strUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.connect();
            InputStreamReader input = new InputStreamReader(httpConn.getInputStream(), "gbk");
            BufferedReader bufReader = new BufferedReader(input);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                contentBuf.append(line);
            }
            bufReader.close();
            httpConn.disconnect();
        } catch (Exception e) {
            System.out.println("链接异常");
        }
        String[] results = contentBuf.toString().split(";");
        for (String result : results) {
            Stocks stocks = new Stocks();
            String[] keyValue = result.split("=");
            String key = keyValue[0].replace("var hq_str_", "");
            String valueStr = keyValue[1].replace("\"", "");
            String[] values = valueStr.split(",");
            if (values.length >= 6) {
                stocks.setName(values[0]);
                // 保留两位小数,上证4位..
                stocks.setPrice((double) Math.round(Double.valueOf(values[1]) * 100) / 100);
                stocks.setChgPrice(Double.valueOf(values[2]));
                stocks.setChgPercent(Double.valueOf(values[3]));
                stocks.setCount(Double.valueOf(values[4]));
                stocks.setMoney(Double.valueOf(values[5]));
                stocks.setCode(key.replace("s_sh", "").replace("s_sz", ""));
                stocks.setAlias(key);
                resultMap.put(key, stocks);
            } else {
                log.warn("sina返回信息:{},无法解析", valueStr);
            }

        }

        return resultMap;

    }
    //删除黑名
//	public void delBlack(){
//		for(int i=0;i<blackMapList.size();i++){
//			String open_id=blackMapList.get(i).get("open_id");
//			ms.moveUser(open_id,"1");
//		}
//	}

    //重新加载列表
    public boolean reLoadList() {
        boolean flag = true;
        stockMapListComp.clear();
        stockMapListChg.clear();
        blackMapList.clear();
        stockMapListChg = dataService.getStockMapListByCorpIdChg(corpId);
        stockMapListComp = dataService.getStockMapListByCorpIdComp(corpId);
        blackMapList = dataService.getBlackList();
        stockListStr = dataService.getStockListStr();
        if (stockListStr == null || stockListStr.length() <= 0) {
            flag = false;
        }
        return flag;
    }

    /**
     * 涨跌幅处理
     *
     * @param currentInfo 当前行情信息
     */
    public void processChg(Map<String, Stocks> currentInfo) {
        for (Map<String, Object> stockMap : stockMapListChg) {

            int stocksId = MapUtils.getInteger(stockMap, "stocks_id");
            String stocksAlias = MapUtils.getString(stockMap, "stocks_alias");
            double changeMin = MapUtils.getDouble(stockMap, "change_min");
            double changeMax = MapUtils.getDouble(stockMap, "change_max");
            String openId = MapUtils.getString(stockMap, "open_id");
            Stocks stocks = currentInfo.get(stocksAlias);
            stocks.setId(stocksId);
            if (stocks.getChgPercent() < changeMin) {
                // 发送跌幅提示
                log.info("change_min.warn.............");
                sendChgMessage(stocks, openId, "-");
                break;//中断
            }
            if (stocks.getChgPercent() > changeMax) {
                // 发送涨幅提示
                log.info("change_max.warn.............");
                sendChgMessage(stocks, openId, "+");
                break;//中断
            }
        }
    }

    /**
     * 发生涨跌幅预警消息
     *
     * @param stocks 股票信息
     * @param openId 微信账号
     * @param flag   涨跌标识
     */
    private void sendChgMessage(Stocks stocks, String openId, String flag) {
        String stocksCode = stocks.getAlias().replace("s_", "");
        StringBuilder msg = new StringBuilder();
        msg.append(simpleDateFormat.format(new Date()));
        msg.append("\n");
        msg.append(stocks.getName());
        if ("+".equals(flag)) {
            msg.append("上涨提示,\n涨幅:");
        } else {
            msg.append("下跌提示,\n涨幅:");
        }
        double chgPercent = stocks.getChgPercent();
        if (chgPercent > 0) {
            msg.append("+");
        }
        msg.append(chgPercent);
        msg.append("%\n");
        msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/");
        msg.append(stocksCode);
        msg.append(".gif\" >分时</a>");

        msg.append(" <a href=\"http://image.sinajs.cn/newchart/daily/n/");
        msg.append(stocksCode);
        msg.append(".gif\" > 日K</a>");
        msg.append(" <a href=\"http://image.sinajs.cn/newchart/weekly/n/");
        msg.append(stocksCode);
        msg.append(".gif\" > 周K</a>");

        msg.append(" <a href=\"http://image.sinajs.cn/newchart/monthly/n/");
        msg.append(stocksCode);
        msg.append(".gif\" > 月K</a>");

        messageService.sendMessage(agentId, openId, msg.toString(), true);
        if (dataService.updateChange(stocks.getId(), flag) && reLoadList()) {
            System.out.println("data process success....");
        } else {
            System.out.println("data process failed....");
        }
    }


    /**
     * 对比处理
     *
     * @param currentInfo 当前行情信息
     */
    public void processComp(Map<String, Stocks> currentInfo) {
        for (Map<String, Object> stockMap : stockMapListComp) {

            int stocksId = MapUtils.getInteger(stockMap, "stocks_id");
            String stocksAlias = MapUtils.getString(stockMap, "stocks_alias");
            String stocksAliasComp = MapUtils.getString(stockMap, "stocks_alias_comp");
            String openId = MapUtils.getString(stockMap, "open_id");
            Stocks stocks = currentInfo.get(stocksAlias);
            stocks.setId(stocksId);
            Stocks stocksComp = currentInfo.get(stocksAliasComp);

            //如果比较代码为空或者比较代码的当前信息为空，跳出此次循环
            if (stocksAliasComp == null || stocksComp == null) {
                continue;
            }
            // 差异提示处理
            boolean diffWarnFlag = true;
            //Date diffWarnTime = (Date) MapUtils.getObject(stockMap, "diff_warn_time");
            // long minutes = (System.currentTimeMillis() - diffWarnTime.getTime()) / (1000 * 60);
            // 小于5分钟不进行差异提示
            //if (minutes < 5) {
            //    diffWarnFlag = false;
            //}

            String diffWarnProcessFlag = MapUtils.getString(stockMap, "diff_warn_process_flag");
            // 预警未处理不再提示
            if ("N".equals(diffWarnProcessFlag)) {
                diffWarnFlag = false;
            }
            if (diffWarnFlag) {
                double stocksPrice = stocks.getPrice();
                double stocksPriceComp = stocksComp.getPrice();
                double priceInit = MapUtils.getDouble(stockMap, "stocks_price_init");
                double priceInitComp = MapUtils.getDouble(stockMap, "stocks_price_init_comp");
                if (stocksPrice > 0 && stocksPriceComp > 0 && priceInit > 0 && priceInitComp > 0) {
                    double baseMultiple = MapUtils.getDouble(stockMap, "base_multiple");
                    double baseDiff = getDiff(priceInit, priceInitComp, baseMultiple);
                    double realDiff = getDiff(stocksPrice, stocksPriceComp, baseMultiple);
                    double diffRange = MapUtils.getDouble(stockMap, "diff_range");
                    if (realDiff > (baseDiff + diffRange)) {
                        log.info("up.warn.............");
                        // 当前差异大于1时,差异变大说明stocks上涨,所以...
                        if (realDiff > 1) {
                            sendCompMessage(realDiff, stocks, stocksComp, openId, "+");
                        } else {
                            sendCompMessage(realDiff, stocks, stocksComp, openId, "+");
                        }
                        break;
                    }
                    if (realDiff < (baseDiff - diffRange)) {
                        log.info("down.warn.............");
                        // 当前差异大于1时,差异变小说明stocks下跌,所以...
                        if (realDiff > 1) {
                            sendCompMessage(realDiff, stocks, stocksComp, openId, "-");
                        } else {
                            sendCompMessage(realDiff, stocks, stocksComp, openId, "-");
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 发送对比预警消息
     *
     * @param realDiff   当前差异
     * @param stocks     股票信息
     * @param stocksComp 对比股票信息
     * @param openId     用户
     * @param flag       涨跌标识
     */
    private void sendCompMessage(double realDiff, Stocks stocks, Stocks stocksComp, String openId, String flag) {
        String stocksCode = stocks.getAlias().replace("s_", "");
        String stocksCompCode = stocksComp.getAlias().replace("s_", "");
        //msg.append(stocks.getCode());
        //msg.append(stocks_comp.getCode());
        String msg = simpleDateFormat.format(new Date()) +
                "\n" +
                "差异:" +
                df.format(realDiff) +
                "%\n" +
                "卖出:" +
                " <a href=\"http://image.sinajs.cn/newchart/min/n/" +
                stocksCode +
                ".gif\" >" +
                stocks.getName() +
                "</a>" +
                stocks.getPrice() +
                "," +
                stocks.getChgPercent() +
                "%\n" +
                "买入:" +
                " <a href=\"http://image.sinajs.cn/newchart/min/n/" +
                stocksCompCode +
                ".gif\" >" +
                stocksComp.getName() +
                "</a>" +
                stocksComp.getPrice() +
                "," +
                stocksComp.getChgPercent() +
                "% \n";
        messageService.sendMessage(agentId, openId, msg, true);
        if (dataService.updateDiffWarnTime(stocks.getId()) && reLoadList()) {
            System.out.println("data process success....");
        } else {
            System.out.println("data process failed....");
        }
    }

    /**
     * 获取价格差异
     *
     * @param price1   价格1
     * @param price2   价格2
     * @param multiple 倍数
     */
    private double getDiff(double price1, double price2, double multiple) {
        // 倍数处理
        if (price1 / price2 > 2 || price2 / price1 > 2) {
            // 相差2倍以上,则启用倍数调整
            if (price1 < price2) {
                price1 = price1 * multiple;
            } else {
                price1 = price1 / multiple;
            }
        }
        // 2倍差值/价格和
        return 2 * (price1 - price2) / (price1 + price2) * 100;
    }

}
