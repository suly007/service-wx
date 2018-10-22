package app.message.resp;

import lombok.Data;

/**
 * 企业微信被动消息类
 *
 * @author: Huanqd@2018-10-18 16:14
 */
@Data
public class PassiveMessage {
    // 成员UserID
    private String ToUserName;
    // 企业微信CorpID
    private String FromUserName;
    // 消息创建时间（整型）
    private long CreateTime;
    // 消息类型，此时固定为：text
    private String MsgType = "text";
    // 文本消息内容
    private String Content;

    // 消息签名
    private String MsgSignature;
    // 时间戳
    private String TimeStamp;
    // 随机数，由企业自行生成
    private String Nonce;

}
