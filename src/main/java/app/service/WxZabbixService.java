package app.service;

import app.message.resp.PassiveMessage;
import app.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * 核心服务类
 *
 * @author liufeng
 * @date 2013-05-20
 */
@Slf4j
@Service
public class WxZabbixService {

    private StocksService stocksService;


    @Autowired
    public WxZabbixService(StocksService stocksService) {
        this.stocksService = stocksService;
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
        PassiveMessage passiveMessage = new PassiveMessage();
        try {

           // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");
            // 回复文本消息
            passiveMessage.setToUserName(fromUserName);
            passiveMessage.setFromUserName(toUserName);
            passiveMessage.setCreateTime(System.currentTimeMillis());
            passiveMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
            // passiveMessage.setFuncFlag(0);
            passiveMessage.setContent(stocksService.process(requestMap));
        } catch (Exception e) {
            log.error("处理微信post请求失败,", e);
        }

        return passiveMessage;
    }
}