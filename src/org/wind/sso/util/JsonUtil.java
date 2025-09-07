package org.wind.sso.util;

import java.io.IOException;

import org.wind.tool.third.jackson.JsonParseException;
import org.wind.tool.third.jackson.map.JsonMappingException;
import org.wind.tool.third.jackson.map.ObjectMapper;
import org.wind.tool.third.jackson.map.SerializationConfig;

/**
 * @描述 : JSON工具类（jackson）
 * @版权 : 胡璐璐
 * @时间 : 2019年11月22日 16:02:16
 */
@SuppressWarnings("unchecked")
public class JsonUtil {
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Description :toJson,对象转换成JSON字符串
	 * @param o 要转换的对象
	 * @return 返回转换后的字符串
	 * @throws Exception 
	 */
	public static String toJson(Object o) {
		try {
			objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
			return objectMapper.writeValueAsString(o);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	/**
	 * Description :toObject,JSON字符串转换成对像
	 * @param json  要转换的JSON字符串
	 * @param t 转换成对像类和CLASS
	 * @return 转成的对像
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static <T> T toObject(String json, Class<?> t)  {
		Object result=null;
		if(json!=null){
			try{
				Object obj=objectMapper.readValue(json, t);
				if(obj==null){
					obj=t.newInstance();
				}
				result=obj;
			}catch(Exception e){
				try {result=t.newInstance();} catch (Exception e1) {}
			}
		}
		return (T) result;
	}
}