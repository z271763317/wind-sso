package org.wind.sso.util;

import java.util.List;

import org.apache.http.Header;

/**
 * @描述 : HttpClient接收返回数据HttpResponse对象的封装类
 * @版权 : 鼎好国际
 * @修改 : 鼎好国际——胡璐璐 
 * @时间 : 2015年1月15日 11:56:15
 */
public class Response {
	
	private int statusCode;																			//httpClient执行模拟请求的状态码
	private String html;																				//HttpEntity返回的html、xml、json等
	private String responseAsString = null;
	private List<Header> cookies;
	private String location;																			//301、302等跳转的URL

	public Response(int statusCode) {
		this.statusCode = statusCode;
	}
	public Response(int statusCode, String html) {
		this(statusCode);
		this.html = html;
	}
	public Response(int statusCode, String html, List<Header> cookies) {
		this(statusCode);
		this.html = html;
		this.cookies = cookies;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public String asString() throws HttpClientExecuteException {
		if(this.html!= null) {
			return this.html;
		} else {
			throw new IllegalArgumentException("html is null");
		}
	}
	public List<Header> getCookies() {
		return this.cookies;
	}

	@Override
	public String toString() {
		if (null != responseAsString) {
			return responseAsString;
		}
		return "Response{" + "statusCode=" + statusCode + ", responseString='"
				+ responseAsString + '\'' + '}';
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
