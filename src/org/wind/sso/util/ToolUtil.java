package org.wind.sso.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @描述 : 通用工具类
 * @版权 : 胡璐璐
 * @时间 : 2015年10月9日 09:38:45
 */
public final class ToolUtil {

//	private static final Logger logger=Logger.getLogger(ToolUtil.class);

	/** 获取 : 请求者IP **/
	public static String getRequestIP(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	/** 获取 : 指定key的cookie值 **/
	public static String getCookieValue(HttpServletRequest request, String key) {
		if (key == null) {
			return null;
		}
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (key.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	/** 设置 : 指定key的cookie值 **/
	public static void setCookieValue(HttpServletResponse response,String key,String value,Integer expiry,Boolean isHttpOnly) {
		Cookie cookie=new Cookie(key, value);
		if(expiry!=null) {
			cookie.setMaxAge(expiry);
		}
		if(isHttpOnly!=null) {
			cookie.setHttpOnly(isHttpOnly);
		}
		response.addCookie(cookie);
	}
	/**获取 : 本机所有的ip **/
	public static List<String> getAllIp() throws SocketException {
		List<String> ipList=new ArrayList<String>();
		Enumeration<NetworkInterface> interfs = NetworkInterface.getNetworkInterfaces();
		while (interfs.hasMoreElements()){
			NetworkInterface interf = interfs.nextElement();
			Enumeration<InetAddress> addres = interf.getInetAddresses();
			while (addres.hasMoreElements()){
				InetAddress in = addres.nextElement();
				ipList.add(in.getHostAddress());
			}
		}
		return ipList;
	}
}