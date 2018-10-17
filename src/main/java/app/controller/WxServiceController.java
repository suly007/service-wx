package app.controller;

import app.service.CoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 微信请求核心处理类
 *
 * @author: Huanqd@2018-10-17 08:47
 */
@RestController
@Slf4j
public class WxServiceController {

    @Autowired
    private CoreService coreService;

    /**
     *
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     */
    @GetMapping("/wxService")
    public String sign(String signature,
                       String timestamp,
                       String nonce,
                       String echostr) {

        log.info("来自微信get请求,signature:{},timestamp:{},nonce:{},echostr:{}", signature, timestamp, nonce, echostr);
        return echostr;
    }


    @PostMapping("/wxService")
    public String process(HttpServletRequest request) {

        log.info("来自微信post请求");
        String msg = coreService.processRequest(request);
        return msg;
    }





}
