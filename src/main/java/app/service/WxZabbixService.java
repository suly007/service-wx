package app.service;

import app.message.corp.PassiveMessage;
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
    //static DBServiceBO dbService = new DBServiceBO();


    private Monitor monitor;

    private DataService dataService;

    private StocksService stocksService;


    @Autowired
    public WxZabbixService(Monitor monitor, DataService dataService, StocksService stocksService) {
        this.monitor = monitor;
        this.dataService = dataService;
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
        String respMessage = null;
        PassiveMessage passiveMessage = new PassiveMessage();
        try {

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



            passiveMessage.setContent(stocksService.process(requestMap));
            //respMessage = MessageUtil.textMessageToXml(passiveMessage);

            //log.info("返回给用户的消息:{}", respMessage);
        } catch (Exception e) {
            log.error("处理微信post请求失败,", e);
        }

        return passiveMessage;
    }
}