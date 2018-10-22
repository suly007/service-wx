package app.pojo;


import lombok.Data;

/**
 * 企业号发送消息返回结果
 *
 * @author: Huanqd@2018-10-22 11:57
 */
@Data
public class SendResult {
        private String errcode;
        private String errmsg;
        private String invaliduser;


        public SendResult(String errcode, String errmsg) {
        super();
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public String getErrcode() {
        return errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public String getInvaliduser() {
        return invaliduser;
    }
    }