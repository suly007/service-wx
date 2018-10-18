package app.service;

import app.message.corp.PassiveMessage;
import app.pojo.Stocks;
import app.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * 核心服务类
 *
 * @author liufeng
 * @date 2013-05-20
 */
@Slf4j
@Service
public class WxZabbixService {
    //static DBServiceBO dbService = new DBServiceBO();
    static String baseURL = "http://hq.sinajs.cn/";

    private Monitor monitor;

    private DataService dataService;

    static Random r = new Random();
    public final static Map<String, String> messageMap = new HashMap<String, String>();

    static {
        messageMap.put("ErrorStock", "股票代码错误!");
        messageMap.put("Manual", "回复“股票代码“查股价\n回复#查询自选股行情\n回复##查询圈共享股行情\n回复“+股票代码“增加自选股\n回复“-(.)股票代码“删除自选股\n回复“-(.)#“删除全部自选股");
    }

    @Autowired
    public WxZabbixService(Monitor monitor, DataService dataService) {
        this.monitor = monitor;
        this.dataService = dataService;
    }

    /**
     * 处理微信发来的请求
     *
     * @param requestMap
     * "Content":"[微笑]",
     * "CreateTime":"1539852076",
     * "ToUserName":"wwe86dd806f53d5ead",
     * "FromUserName":"XunQingDong",
     * "MsgType":"text",
     * "AgentID":"1000002",
     * "MsgId":"228896306"
     *
     * @return
     */
    public PassiveMessage processRequest(Map<String,String> requestMap) {
        String respMessage = null;
        PassiveMessage passiveMessage = new PassiveMessage();
        try {
            StringBuilder respContent = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            respContent.append(sdf.format(new Date()));
            respContent.append("\n");
            // xml请求解析
            log.info("requestMap:{}",requestMap);



//
//            {       ToUserName=wwe86dd806f53d5ead,
//                    AgentID=1000002,
//                    Encrypt=tEAN9AyppVIL8QFuMM6KXMBzVeKFD2tmpdGmlVt+E20No9QDdZJCCt9w/UYtNnkslB8mxA4gZbaP4Ga9RHC8nWZ53Ygots3n7OxLPk2mvZth2YIVKwmwYy6z+ygdAwQCGfU8094mYZlXhSirfOSTkEbqkPz1YDFP+5P8TAEcgxSef0v9Y9aGLpT2hrc4Kw9npnBsu7sND7preaHW5zBTCg/rDiVD3LH46P3cFWXyfDShjPFI80vxQ9uI0c2/cE2q1PDouCuX7flNVsROABLW9yy3ltPbGebOcDO705b448r/TsI4CHrQVcnCQ0f+RKjaU83a6z3mhwqpjcnvPERGsPXVciO6U+mK6QFf9WtkuKW2uUIYh8GjUXfn/vokh82TRaA6CqVJjqs6nAD0CDrO8uxmod7D620nukgnP4HarNc=}

           // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");

            // 消息类型
            String msgType = requestMap.get("MsgType");
            String Content = requestMap.get("Content");
            log.info("消息处理：fromUserName:" + fromUserName + ",toUserName:" + toUserName + ",msgType:" + msgType
                    + "Content:" + Content);
            // 回复文本消息
            passiveMessage.setToUserName(fromUserName);
            passiveMessage.setFromUserName(toUserName);
            passiveMessage.setCreateTime(System.currentTimeMillis());
            passiveMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
            // passiveMessage.setFuncFlag(0);

            Map<String, String> actionMap = ProcessContent(Content, msgType, fromUserName);
            String action = actionMap.get("action");
            String actionType = actionMap.get("actionType");
            // 股票代码处理
            if (actionType == "" || actionType == null || action == null || action == "") {
                respContent.append("未能解析发送内容");
            } else if ("Message".equals(actionType)) {
                respContent.append(messageMap.get(action));
            } else if ("SinaStock".equals(actionType)) {
                respContent.append(ProcessStocks(action));
            } else if ("AddStock".equals(actionType)) {
                respContent.append(addCodeByUser(action, fromUserName, toUserName));
            } else if ("DelStock".equals(actionType)) {
                respContent.append(delCodeByUser(action, fromUserName, toUserName));
            }else if("DelAllStock".equals(actionType)){
				respContent.append(delAllByUser(action, fromUserName, toUserName));
			}else if("ChgBaseDiff".equals(actionType)){
				respContent.append(chgBaseDiffByUser(action, fromUserName, toUserName));
			}

            passiveMessage.setContent(respContent.toString());
            //respMessage = MessageUtil.textMessageToXml(passiveMessage);

            //log.info("返回给用户的消息:{}", respMessage);
        } catch (Exception e) {
            log.error("处理微信post请求失败,", e);
        }

        return passiveMessage;
    }

    public String addCodeByUser(String content, String open_id, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (content != null && content.length() == 10 && content.charAt(0) == 's') {
            String stocks_code = content.replace("s_sh", "").replace("s_sz", "");
            if (stocks_code.length() == 6) {
                if (dataService.notExist(stocks_code, open_id, appid)) {
                    if (dataService.insertData(stocks_code, content, open_id, appid)) {
                        resulet = "操作成功!";
                    }
                } else {
                    resulet = "相关代码已经存在,无需重复添加!";
                }
            }
        }
        return resulet;
    }

    public String delCodeByUser(String content, String open_id, String appid) {
        String resulet="未找到相关数据或解析出错!";
        if (content != null && content.length() == 10 && content.charAt(0) == 's') {
            String stocks_code = content.replace("s_sh", "").replace("s_sz", "");
            if (stocks_code.length() == 6) {
                if (dataService.delExist(stocks_code, open_id, appid)) {
                    resulet="操作成功!";
                }
            }
        }
        return resulet;
    }

    public String delAllByUser(String content, String open_id, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (dataService.delAllExist(open_id, appid)) {
            resulet = "操作成功!";
        }
        return resulet;
    }

    public String chgBaseDiffByUser(String content, String open_id, String appid) {
        String resulet = "未找到相关数据或解析出错!";
        if (dataService.chgBaseDiffByUser(open_id, appid)) {
            resulet = "操作成功!";
        }
        return resulet;
    }
    //根据open_id获取自选股列表
	public String getStocksListStr(String open_id) {
		String stocksListStr = dataService.getStockListStrByOpenID(open_id);
		if (stocksListStr == null || stocksListStr.length() < 9) {
			stocksListStr = dataService.getStockListStr();
		}
		return stocksListStr;
	}
	
	//根据open_id获取自选股列表
	public String getStocksListStr() {
		return dataService.getStockListStr();
	}

    public Map<String, String> ProcessContent(String content, String msgType, String fromUserName) {
        Map<String, String> resMap = new HashMap<String, String>();
        String actionType = "";
        String action = "";
        // 文本消息
        if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT) && content != null) {
            log.info("文本内容：" + content);
            if ("ok".equalsIgnoreCase(content)) {

                actionType = "ChgBaseDiff";
                action = content;
            } else if ("zx".equalsIgnoreCase(content) || "#".contentEquals(content)) {
                actionType = "SinaStock";
                action=getStocksListStr(fromUserName);
            } else if ("all".equalsIgnoreCase(content) || "##".endsWith(content)) {
                actionType = "SinaStock";
                action=getStocksListStr();
            } else if (".#".equals(content) || ("-#".equals(content))) {
                actionType = "DelAllStock";
                action = content;
            } else if (content.length() == 6) {// 长度为6 解析为stocks
                String stockCode = processStockCode(content);
                if (stockCode.contains("s_sh") || stockCode.contains("s_sz")) {
                    actionType = "SinaStock";
                    action = stockCode;
                } else {
                    actionType = "Message";
                    action = "ErrorStock";
                }
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
            } else {
                actionType = "Message";
                action = "Manual";
            }
        } else {
            log.info("非文本内容：" + content);
            // 非文本类型
            actionType = "Message";
            action = "Manual";
        }
        resMap.put("actionType", actionType);
        resMap.put("action", action);
        log.info("解析用户消息:{},解析结果:{}", content, resMap);
        return resMap;
    }

    public String ProcessStocks(String action) {
        // 默认返回的文本消息内容
        StringBuilder respContent = new StringBuilder();
        StringBuilder url = new StringBuilder();
        url.append(baseURL);
        url.append("rn=");
        url.append(r.nextLong());
        url.append("&list=");
        url.append(action);
        Map<String, Stocks> currentInfo = monitor.getCurrentInfo(url.toString());//sortMapByValue(monitor.getCurrentInfo(url.toString()));
        int count = currentInfo.keySet().size();
        for (String key : currentInfo.keySet()) {
            Stocks s = currentInfo.get(key);
            if (count > 1) {
                double percent = s.getChgpercent();
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
                respContent.append(s.getChgpercent());
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


    public static String processStockCode(String Content) {
        String stockCode = "";
        if (Content.charAt(0) == '6') {
            stockCode = "s_sh" + Content;
        } else if (Content.charAt(0) == '0' || Content.charAt(0) == '3') {
            stockCode = "s_sz" + Content;
        }
        return stockCode;
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
