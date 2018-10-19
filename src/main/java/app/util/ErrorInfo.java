package app.util;

import lombok.Data;

@Data
public class ErrorInfo {
	private String errcode;
	private String errmsg;
	
	public ErrorInfo(String errcode, String errmsg) {
		super();
		this.errcode = errcode;
		this.errmsg = errmsg;
	}

}
