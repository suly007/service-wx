package app.service;

import app.pojo.Stocks;
import app.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 公用服务类
 *
 * @author Huanqd@2018-10-19 10:09
 */
@Slf4j
@Service
public class StocksService {

    private DataService dataService;

    private MonitorService monitorService;

    @Autowired
    public StocksService(DataService dataService, MonitorService monitorService) {
        this.dataService = dataService;
        this.monitorService = monitorService;
    }

    private static final Map<String, String> MESSAGE_MAP = new HashMap<String, String>();

    static {
        MESSAGE_MAP.put("ErrorStock", "股票代码错误!");
        MESSAGE_MAP.put("Manual", "回复“股票代码“查股价\n回复#查询自选股行情\n回复##查询圈共享股行情\n回复“+股票代码“增加自选股\n回复“-(.)股票代码“删除自选股\n回复“-(.)#“删除全部自选股");
    }


    private String delAllByUser(String content, String openId, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (dataService.delAllExist(openId, appid)) {
            resulet = "操作成功!";
        }
        return resulet;
    }

    private String ChgInitPrice(String content, String openId, String appId) {
        String result = "未找到相关数据或解析出错!";
        try {
            content = content.toLowerCase().replace("ok", "");
            String[] prices = content.split("/");
            if (prices.length == 2) {
                double priceInit = Double.valueOf(prices[0]);
                double priceInitComp = Double.valueOf(prices[1]);
                int count = dataService.chgInitPriceByUser(openId, appId, priceInit, priceInitComp);
                result = "+" + count + "操作成功!" + priceInit + "/" + priceInitComp;
            } else {
                int count = dataService.chgInitPriceByUser(openId, appId);
                result = "+" + count + "操作成功!";
            }
        } catch (Exception e) {
            log.error("ChgInitPrice发生异常,content:{},openId:{},appId:{}", content, openId, appId);
            result = e.getMessage();
        }
        return result;
    }


    private String addCodeByUser(String content, String openId, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (content != null && content.length() == 10 && content.charAt(0) == 's') {
            String stocks_code = content.replace("s_sh", "").replace("s_sz", "");
            if (stocks_code.length() == 6) {
                if (dataService.notExist(stocks_code, openId, appid)) {
                    if (dataService.insertData(stocks_code, content, openId, appid)) {
                        resulet = "操作成功!";
                    }
                } else {
                    resulet = "相关代码已经存在,无需重复添加!";
                }
            }
        }
        return resulet;
    }

    private String delCodeByUser(String content, String openId, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (content != null && content.length() == 10 && content.charAt(0) == 's') {
            String stocks_code = content.replace("s_sh", "").replace("s_sz", "");
            if (stocks_code.length() == 6) {
                if (dataService.delExist(stocks_code, openId, appid)) {
                    resulet = "操作成功!";
                }
            }
        }
        return resulet;
    }


    private String processStocks(String action) {
        // 默认返回的文本消息内容
        StringBuilder respContent = new StringBuilder();
        StringBuilder url = new StringBuilder();
        url.append("http://hq.sinajs.cn/rn=");
        url.append(RandomUtils.nextLong());
        url.append("&list=");
        url.append(action);
        Map<String, Stocks> currentInfo = monitorService.getCurrentInfo(url.toString());//sortMapByValue(monitor.getCurrentInfo(url.toString()));
        int count = currentInfo.keySet().size();
        for (String key : currentInfo.keySet()) {
            Stocks s = currentInfo.get(key);
            if (count > 1) {
                double percent = s.getChgPercent();
                String space;
                String percentStr;
                if (percent >= 0) {
                    space = "-";//"↑";
                    percentStr = "+" + percent;
                } else {
                    space = "-";//"↓";
                    percentStr = String.valueOf(percent);
                }
                respContent.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/");
                respContent.append(key.replace("s_", ""));
                respContent.append(".gif\" >");
                String name = s.getName().replace(" ", "");
                for (int i = 0; i < 8 - name.getBytes().length; i++) {
                    respContent.append(space);
                }
                respContent.append(name);

                respContent.append("</a>");
                String price = String.valueOf(s.getPrice());
                respContent.append("  ");
                respContent.append(s.getPrice());
                for (int i = 0; i < 6 - price.getBytes().length; i++) {
                    respContent.append("0");
                }
                respContent.append(",");
                respContent.append("  ");
                respContent.append(percentStr);
                for (int i = 0; i < 6 - percentStr.getBytes().length; i++) {
                    respContent.append("0");
                }
                respContent.append("%\n");
            } else if (count == 1) {
                respContent.append(s.getName());
                respContent.append("(");
                respContent.append(s.getCode());
                respContent.append(")");
                respContent.append("\n价格:");
                respContent.append(s.getPrice());
                respContent.append(",涨幅:");
                respContent.append(s.getChgPercent());
                respContent.append("%\n");
                respContent.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/");
                respContent.append(key.replace("s_", ""));
                respContent.append(".gif\" >分时</a>");

                respContent.append(" <a href=\"http://image.sinajs.cn/newchart/daily/n/");
                respContent.append(key.replace("s_", ""));
                respContent.append(".gif\" > 日K</a>");
                respContent.append(" <a href=\"http://image.sinajs.cn/newchart/weekly/n/");
                respContent.append(key.replace("s_", ""));
                respContent.append(".gif\" > 周K</a>");

                respContent.append(" <a href=\"http://image.sinajs.cn/newchart/monthly/n/");
                respContent.append(key.replace("s_", ""));
                respContent.append(".gif\" > 月K</a>");

            }
        }
        return respContent.toString();
    }


    private static String processStockCode(String Content) {
        String stockCode = "";
        if (Content.charAt(0) == '6') {
            stockCode = "s_sh" + Content;
        } else if (Content.charAt(0) == '0' || Content.charAt(0) == '3') {
            stockCode = "s_sz" + Content;
        }
        return stockCode;
    }

    //根据openId获取自选股列表
    private String getStocksListStr(String openId) {
        String stocksListStr = dataService.getStockListStrByOpenID(openId);
        if (stocksListStr == null || stocksListStr.length() < 9) {
            stocksListStr = dataService.getStockListStr();
        }
        return stocksListStr;
    }

    //根据openId获取自选股列表
    private String getStocksListStr() {
        return dataService.getStockListStr();
    }

    private Map<String, String> getActionMap(String content, String fromUserName) {
        Map<String, String> resMap = new HashMap<String, String>();
        String actionType = "";
        String action = "";
        String way = "";
        if (content.startsWith("ok") || content.startsWith("OK")) {
            // 修改比价的基础价格
            actionType = "ChgInitPrice";
            action = content;
            way = "ok";
        } else if ("zx".equalsIgnoreCase(content) || "#".equals(content)) {
            actionType = "SinaStock";
            action = getStocksListStr(fromUserName);
            way = "zx";
        } else if ("all".equalsIgnoreCase(content) || "##".equalsIgnoreCase(content)) {
            actionType = "SinaStock";
            action = getStocksListStr();
            way = "all";
        } else if (".#".equals(content) || ("-#".equals(content))) {
            actionType = "DelAllStock";
            action = content;
            way = ".#";
        } else if (content.length() == 6) {// 长度为6 解析为stocks
            String stockCode = processStockCode(content);
            if (stockCode.contains("s_sh") || stockCode.contains("s_sz")) {
                actionType = "SinaStock";
                action = stockCode;
            } else {
                actionType = "Message";
                action = "ErrorStock";
            }
            way = "6";
        } else if (content.length() == 7) {
            String stockCode = processStockCode(content.substring(1, 7));
            actionType = "Message";
            action = "ErrorStock";
            if (stockCode.length() == 10) {
                if (content.charAt(0) == '+') {
                    actionType = "AddStock";
                    action = stockCode;
                } else if (content.charAt(0) == '-' || content.charAt(0) == '.') {
                    actionType = "DelStock";
                    action = stockCode;
                }
            }
            way = "7";
        } else {
            actionType = "Message";
            action = "Manual";
            way = "else";
        }

        resMap.put("actionType", actionType);
        resMap.put("action", action);
        resMap.put("way", way);
        log.info("解析用户消息:[{}],解析结果:[{}]", content, resMap);
        return resMap;
    }

    public String process(Map<String, String> requestMap) {
        // 发送方帐号（openId）
        String fromUserName = requestMap.get("FromUserName");
        // 公众帐号
        String toUserName = requestMap.get("ToUserName");

        // 消息类型
        String msgType = requestMap.get("MsgType");

        String content = "";
        if (MessageUtil.REQ_MESSAGE_TYPE_EVENT.equals(msgType)) {
            // 自定义菜单事件
            //EventKey=#, Event=click;
            String eventType = MapUtils.getString(requestMap, "Event");
            if (MessageUtil.EVENT_TYPE_CLICK.equals(eventType)) {
                String eventKey = MapUtils.getString(requestMap, "EventKey");

                content = eventKey;
            }

        } else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
            content = requestMap.get("Content");
            log.info("文本内容：" + content);
        }

        if ("".equals(content)) {
            log.info("用户内容不进行解析~~~~~~~~~~~~~");
            return "";
        }

        log.info("消息处理：fromUserName:" + fromUserName + ",toUserName:" + toUserName + ",msgType:" + msgType
                + "Content:" + content);
        StringBuilder respContent = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        respContent.append(sdf.format(new Date()));
        respContent.append("\n");
        Map<String, String> actionMap = getActionMap(content, fromUserName);
        String action = actionMap.get("action");
        String actionType = actionMap.get("actionType");
        // 股票代码处理
        if (StringUtils.isBlank(actionType) || StringUtils.isBlank(action)) {
            respContent.append("未能解析发送内容");
        } else if ("Message".equals(actionType)) {
            respContent.append(MESSAGE_MAP.get(action));
        } else if ("SinaStock".equals(actionType)) {
            respContent.append(processStocks(action));
        } else if ("AddStock".equals(actionType)) {
            respContent.append(addCodeByUser(action, fromUserName, toUserName));
        } else if ("DelStock".equals(actionType)) {
            respContent.append(delCodeByUser(action, fromUserName, toUserName));
        } else if ("DelAllStock".equals(actionType)) {
            respContent.append(delAllByUser(action, fromUserName, toUserName));
        } else if ("ChgInitPrice".equals(actionType)) {
            respContent.append(ChgInitPrice(action, fromUserName, toUserName));
        }

        return respContent.toString();
    }


    /*public Map<String, Stocks> sortMapByValue(Map<String, Stocks> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, Stocks> sortedMap = new LinkedHashMap<String, Stocks>();
        List<Entry<String, Stocks>> entryList = new ArrayList<Map.Entry<String, Stocks>>(
                oriMap.entrySet());
        Collections.sort(entryList, new Comparator(){
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				Entry<String, Stocks> m1=(Entry<String, Stocks>)o1;
				Entry<String, Stocks> m2=(Entry<String, Stocks>)o2;
				return m1.getValue().getChgpercent()>m2.getValue().getChgpercent()?-1:1;
			}
        });
        Iterator<Entry<String, Stocks>> iter = entryList.iterator();
        Entry<String, Stocks> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }*/
}


// // 图片消息
// else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
// respContent = "您发送的是图片消息！";
// }
// // 地理位置消息
// else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
// respContent = "您发送的是地理位置消息！";
// }
// // 链接消息
// else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
// respContent = "您发送的是链接消息！";
// }
// // 音频消息
// else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
// respContent = "您发送的是音频消息！";
// }
// // 事件推送
// else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
// // 事件类型
// String eventType = requestMap.get("Event");
// // 订阅
// if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
// respContent = "谢谢您的关注！";
// }
// // 取消订阅
// else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
// // TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
// }
// // 自定义菜单点击事件
// else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
// // TODO 自定义菜单权没有开放，暂不处理该类消息
// }
// }
