package app.util;

public class ErrorInfo {
	private String errcode;
	private String errmsg;
	
	public ErrorInfo(String errcode, String errmsg) {
		super();
		this.errcode = errcode;
		this.errmsg = errmsg;
	}
	public String getErrcode() {
		return errcode;
	}
	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	
}
