package org.wind.sso.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @描述 : 正则表达式工具类
 * @版权 : 胡璐璐
 * @时间 : 2019年11月22日 16:10:47
 */
public final class RegexUtil {

	/**清除左右空格的数据，如果长度为0，则返回null**/
	public static String clearEmpty(String str){
		return str!=null && str.trim().length()>0?str.trim():null;
	}
	 /**判断str是否为ip地址**/
	public static boolean isIP(String addr) {
		if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
			return false;
		}
		//判断IP格式和范围
		String regex ="([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		return addr.matches(regex);
	}
	/**获取当前日期时间（标准式，格式：yyyy-MM-dd HH:mm:ss）*/
	public static String getDate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	/**
	 * 计算指定的时间和当前时间的差距（以毫秒为单位）
	 * @throws ParseException 
	 */
	public static Long getDataDifference(String date) throws ParseException {
		return getDataDifference(RegexUtil.getDate(),date);
	}
	/**
	 * 计算指定的2个时间的差距（以毫秒为单位）
	 * @throws ParseException 
	 */
	public static Long getDataDifference(String beginTime,String endTime) throws ParseException {
		long between = 0;
		SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date begin = dfs.parse(beginTime);
		Date end = dfs.parse(endTime);
		between = end.getTime() - begin.getTime() ;// 除以1000是为了转换成秒
		return between;
	}
}