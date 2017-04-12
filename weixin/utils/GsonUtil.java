
package com.fs.module.weixin.utils;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
/**
 * 
 * @author wangkai
 * @2016年7月20日 下午7:12:06
 * @desc:gson for java 序列化工具
 */
public class GsonUtil {

	private static Gson gson = null;
	
	static {
		if (gson == null) {
			//gson = new Gson();
			gson = new GsonBuilder().create();
		}
	}

	private GsonUtil() {
	}

	/**
	 * 转成json
	 * 
	 * @param object
	 * @return
	 */
	public static String GsonString(Object object) {
		String gsonString = null;
		if (gson != null) {
			gsonString = gson.toJson(object);
		}
		return gsonString;
	}

	/**
	 * 转成bean
	 * 
	 * @param gsonString
	 * @param cls
	 * @return
	 */
	public static <T> T GsonToBean(String gsonString, Class<T> cls) {
		T t = null;
		if (gson != null) {
			t = gson.fromJson(gsonString, cls);
		}
		return t;
	}

	/**
	 * 转成list
	 * 
	 * @param gsonString
	 * @param cls
	 * @return
	 */
	public static <T> List<T> GsonToList(String gsonString, Class<T> cls) {
		List<T> list = null;
		if (gson != null) {
			list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
			}.getType());
		}
		return list;
	}

	/**
	 * 转成list中有map的
	 * 
	 * @param gsonString
	 * @return
	 */
	public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
		List<Map<String, T>> list = null;
		if (gson != null) {
			list = gson.fromJson(gsonString,
					new TypeToken<List<Map<String, T>>>() {
					}.getType());
		}
		return list;
	}

	/**
	 * 转成map的
	 * 
	 * @param gsonString
	 * @return
	 */
	public static <T> Map<String, T> GsonToMaps(String gsonString) {
		Map<String, T> map = null;
		if (gson != null) {
			map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
			}.getType());
		}
		return map;
	}
}
