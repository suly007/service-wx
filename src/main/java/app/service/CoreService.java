package app.service;

import app.message.resp.TextMessage;
import app.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * 核心服务类
 *
 * @author liufeng
 * @date 2013-05-20
 */
@Slf4j
@Service
public class CoreService {

    @Autowired
    private StocksService stocksService;

    /**
     * 处理微信发来的请求
     *
     * @param request
     * @return
     */
    public String processRequest(HttpServletRequest request) {
        String respMessage = null;
        try {
            // xml请求解析
            Map<String, String> requestMap = MessageUtil.parseXml(request);

            // 发送方帐号（open_id）
            String fromUserName = requestMap.get("FromUserName");
            // 公众帐号
            String toUserName = requestMap.get("ToUserName");
            // 消息类型
            String msgType = requestMap.get("MsgType");
            // 回复文本消息
            TextMessage textMessage = new TextMessage();
            textMessage.setToUserName(fromUserName);
            textMessage.setFromUserName(toUserName);
            textMessage.setCreateTime(System.currentTimeMillis());
            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
            textMessage.setFuncFlag(0);



            textMessage.setContent(stocksService.process(requestMap));
            respMessage = MessageUtil.textMessageToXml(textMessage);
            log.info("返回给用户的消息:{}", respMessage);
        } catch (Exception e) {
            log.error("处理微信post请求失败,", e);
        }

        return respMessage;
    }
}