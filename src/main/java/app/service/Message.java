package app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类
 *
 * @author: Huanqd@2018-10-19 11:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private String agentid;
    private String message;
    private String msgtype;
    private String openId;
    private boolean resend;


}
