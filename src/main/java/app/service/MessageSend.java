package app.service;

import app.pojo.AccessToken;
import app.util.Content;
import app.util.ErrorInfo;
import app.util.Message;
import app.util.UserMapUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author Huanqd
 */
@Service
public class MessageSend {

	@Autowired
	private DataService dataService;

	@Autowired
	private	 AccessTokenService accessTokenService;
	@Autowired
	private RestTemplate restTemplate;

	public synchronized ErrorInfo SendMessage(String message, String msgtype, String openId, boolean resend) {
		AccessToken accessToken= accessTokenService.getSingleAccessToken();
		if(accessToken==null){
			System.out.println("get token failed......");
			dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "get token failed......");
			return new ErrorInfo("801","get token failed......");
		}
		String token = accessToken.getToken();
		String action ="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="+token;
		Message m=new Message();
		m.setTouser(openId);
		m.setMsgtype(msgtype);
		m.setText(new Content(message));
		ErrorInfo errorInfo= post(action,JSON.toJSONString(m));
		dataService.insertMessage(openId, UserMapUtil.userMap.get(openId), message, JSON.toJSONString(errorInfo));
		//如果token失效 清空内存中的token,将数据库token设置为过期,消息重发
		if("40001".equals(errorInfo.getErrcode())){
			accessTokenService.setAccessToken(null);
			dataService.setTokenExpiresd(accessTokenService.getAppid(),accessTokenService.getAppsecret());
			if(resend){
				ErrorInfo resendInfo=this.SendMessage(openId, msgtype, message,false);
				if(!"0".equals(resendInfo.getErrcode())){
					dataService.insertErrorInfo("com.zhx.weixin.service.MessageSend", "SendMessage", "resend message failed......");
				}
			}
		}
		return errorInfo;
	}


	public synchronized ErrorInfo post(String action, String body) {
		ErrorInfo errorInfo;
		try {
			URL url = new URL(action);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("POST");
			http.setRequestProperty("Content-Type",	"application/x-www-form-urlencoded");
			http.setDoOutput(true);
			http.setDoInput(true);
			http.setUseCaches(false);
			System.setProperty("sun.net.client.defaultConnectTimeout", "3000");// 连接超时3秒
			System.setProperty("sun.net.client.defaultReadTimeout", "3000"); // 读取超时3秒
			http.connect();
			OutputStream os = http.getOutputStream();
			os.write(body.getBytes("UTF-8"));// 传入参数
			InputStream is = http.getInputStream();
			int size = is.available();
			byte[] jsonBytes = new byte[size];
			is.read(jsonBytes);
			errorInfo = JSON.parseObject(new String(jsonBytes, "UTF-8"), ErrorInfo.class) ;
			os.flush();
			os.close();
			http.disconnect();
			return errorInfo;
		} catch (Exception e) {
			errorInfo=new ErrorInfo("800","post url catched exception!");
			dataService.insertErrorInfo("com.zhx.weixin.util.HttpProcessUtil", "post", "post url catched exception!");
			return errorInfo;
		}
	}
}