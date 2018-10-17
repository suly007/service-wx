package app.util;

import app.pojo.AccessToken;
import app.service.DataService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 公众平台通用接口工具类
 * 
 * @author liuyq
 * @date 2013-08-09
 */
@Service
public class AccessTokenUtil {
	// private static Logger log = LoggerFactory.getLogger(WeixinUtil.class);
	private static AccessToken accessToken = new AccessToken();
	private static String appid = "wxab36fe6788977907";//测试
	private static String appsecret= "d4624c36b6795d1d99dcf0547af5443d";//测试
	//String appid="wx7175f9658e1378e3";
	//String appsecret = "1be7916598e495b766c7a96f2b71b28f";
	static SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private DataService dataService;

	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param outputStr
	 *            提交的数据
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
	 */
	public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
		JSONObject jsonObject = null;
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod)){
				httpUrlConn.connect();
			}

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			jsonObject = JSON.parseObject(buffer.toString());
		} catch (ConnectException ce) {
			// log.error("Weixin server connection timed out.");
		} catch (Exception e) {
			// log.error("https request error:{}", e);
		}
		return jsonObject;
	}

	// 获取access_token的接口地址（GET） 限200（次/天）
	public final static String access_token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";

	/**
	 * 获取access_token
	 *
	 * @return
	 */
	public AccessToken getSingleAccessToken() {
		if (isTokenExpires(accessToken)) {
			Map<String, Object> token = dataService.getToken(appid, appsecret);
			if (token.isEmpty()) {
				getTokenByHttp(appid,appsecret);
			}else{
				try{
				accessToken.setToken(MapUtils.getString(token,"token"));
				accessToken.setExpiresIn(MapUtils.getInteger(token,"expiresin"));
				accessToken.setExpiresDate((Date)MapUtils.getObject(token,"expiresdate"));
				System.out.println("get token by db......");
				}catch(Exception e){
					 getTokenByHttp(appid,appsecret);
				}
			}
		}else{
			System.out.println("get token by memory......");
		}
		return accessToken;
	}

	public static boolean isTokenExpires(AccessToken accessToken) {
		boolean flag = false;
		Date expiresDate = accessToken.getExpiresDate();
		// 如果此 Timestamp 对象与给定对象相等，则返回值 0；如果此 Timestamp 对象早于给定参数，则返回小于 0 的值；如果此
		// Timestamp 对象晚于给定参数，则返回大于 0 的值。
		if (expiresDate == null || new Date().compareTo(expiresDate) >= 0) {
			flag = true;
		}
		return flag;
	}
	
	public void getTokenByHttp(String appid,String appsecret){
		String requestUrl = access_token_url.replace("APPID", appid).replace("APPSECRET", appsecret);
		JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
		// 如果请求成功
		if (null != jsonObject) {
			try {
				System.out.println("get token by http......");
				accessToken = new AccessToken();
				String token=jsonObject.getString("access_token");
				accessToken.setToken(token);
				int expiresin=jsonObject.getInteger("expires_in");
				accessToken.setExpiresIn(expiresin);
				Date now = new Date();
				now.setTime(now.getTime()+jsonObject.getInteger("expires_in")*1000);
				accessToken.setExpiresDate(now);
				//将token存入数据库
				dataService.insertToken(appid,appsecret,token,String.valueOf(expiresin),sdf.format(now));
			} catch (Exception e) {
				accessToken = null;
				// 获取token失败
				// log.error("获取token失败 errcode:{} errmsg:{}",
				// jsonObject.getInt("errcode"),
				// jsonObject.getString("errmsg"));
			}
		}
	}

	public static AccessToken getAccessToken() {
		return accessToken;
	}

	public static void setAccessToken(AccessToken accessToken) {
		AccessTokenUtil.accessToken = accessToken;
	}

	public static String getAppid() {
		return appid;
	}

	public static void setAppid(String appid) {
		AccessTokenUtil.appid = appid;
	}

	public static String getAppsecret() {
		return appsecret;
	}

	public static void setAppsecret(String appsecret) {
		AccessTokenUtil.appsecret = appsecret;
	}
}