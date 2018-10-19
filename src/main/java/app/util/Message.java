package app.util;

import lombok.Data;

@Data
public class Message {
    private String touser;
    private String msgtype;
    private Content text;
    private String image;
    private String agentid;

}


