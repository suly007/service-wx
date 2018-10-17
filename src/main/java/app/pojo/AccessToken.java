package app.pojo;

import java.util.Date;

/**
 * 微信通用接口凭证
 *  
 * @author liufeng
 * @date 2013-08-08
 */
public class AccessToken {
	// 获取到的凭证
	private String token;
	// 凭证有效时长，单位：秒
	private int expiresIn;
	// 凭证过期时间
	private Date expiresDate;

	public Date getExpiresDate() {
		return expiresDate;
	}

	public void setExpiresDate(Date expiresDate) {
		this.expiresDate = expiresDate;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
}