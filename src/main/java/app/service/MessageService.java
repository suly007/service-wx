package app.service;

import app.util.ErrorInfo;

/**
 * 消息接口
 *
 * @author: Huanqd@2018-10-19 10:57
 */

public interface MessageService {


    public ErrorInfo SendMessage(Message message);



}
