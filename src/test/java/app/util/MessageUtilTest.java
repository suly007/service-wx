package app.util;

import app.message.resp.PassiveMessage;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

/**
 * 类
 *
 * @author: Huanqd@2018-10-18 16:27
 */
@Slf4j
public class MessageUtilTest {

    @Test
    public void textMessageToXml() {

        PassiveMessage passiveMessage = new PassiveMessage();
        passiveMessage.setContent("1212");
        passiveMessage.setMsgSignature("si");
        passiveMessage.setNonce("no");
        passiveMessage.setTimeStamp("ti");
        String res = MessageUtil.textMessageToXml(passiveMessage);
        log.info("res:{}",res);
    }


    @Test
    public void parseXmlStr() throws Exception {
        String xmlStr ="<xml><ToUserName><![CDATA[wwe86dd806f53d5ead]]></ToUserName><FromUserName><![CDATA[XunQingDong]]></FromUserName><CreateTime>1539852076</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[[微笑]]]></Content><MsgId>228896306</MsgId><AgentID>1000002</AgentID></xml>";

        Map<String, String> map = MessageUtil.parseXml(xmlStr);

        log.info("map:{}", JSON.toJSONString(map,true));
    }
}