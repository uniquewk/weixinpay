package com.fs.module.weixin.bean.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.module.weixin.bean.OrderQueryRequestData;
import com.fs.module.weixin.bean.OrderQueryResponseData;
import com.fs.module.weixin.bean.UnifiedOrderNotifyRequestData;
import com.fs.module.weixin.bean.UnifiedOrderRequestData;
import com.fs.module.weixin.bean.UnifiedOrderResponseData;
import com.fs.module.weixin.bean.config.WeixinPayConfig;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

public class WxPayUtil {
	
	//private static Logger logger = LoggerFactory.getLogger(WxPayUtil.class);

	/**
	 * 调用微信统一下单接口
	 * @param data
	 * @return UnifiedOrderResponseData
	 */
	public static UnifiedOrderResponseData unifiedOder(UnifiedOrderRequestData data){
		String requestXMLData = WxPayUtil.castDataToXMLString(data);
		String requestUrl = WeixinPayConfig.WECHAT_UNIFIED_ORDER_URL;
		String requestMethod = "POST";
		//发送请求
		String responseString = WeixinUtil.httpsRequest(requestUrl, requestMethod, requestXMLData);
		//解析响应xml结果数据
		UnifiedOrderResponseData responseData = WxPayUtil.castXMLStringToUnifiedOrderResponseData(responseString);
		return responseData;
	}
	
	/**
	 * 调用订单查询接口
	 * @param data
	 * @return
	 * return:OrderQueryResponseData
	 */
	public static OrderQueryResponseData queryOrder(OrderQueryRequestData data){
		String requestXMLData = WxPayUtil.castDataToXMLString(data);
		String requestUrl = WeixinPayConfig.WECHAT_ORDER_QUERY_URL;
		String requestMethod = "POST";
		String responseString = WeixinUtil.httpsRequest(requestUrl, requestMethod, requestXMLData);
		OrderQueryResponseData responseData = WxPayUtil.castXMLStringToOrderQueryResponseData(responseString);
		return responseData;
	}
	
	/**
	 * 将java对象转换为XML字符串
	 * @param object
	 * @return
	 * return:String
	 */
	private static String castDataToXMLString(Object object){
		//解决XStream对出现双下划线的bug
        XStream xStreamForRequestPostData = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
        //将要提交给API的数据对象转换成XML格式数据Post给API
        return xStreamForRequestPostData.toXML(object);
	}
	
	/**
	 * 把XML字符串转换为统一下单接口返回数据对象
	 * @param responseString
	 * @return
	 * return:UnifiedOrderResponseData
	 */
	private static UnifiedOrderResponseData castXMLStringToUnifiedOrderResponseData(String responseString){
		XStream xStream = new XStream(new DomDriver());
		xStream.alias("xml", UnifiedOrderResponseData.class);
		xStream.processAnnotations(UnifiedOrderResponseData.class);
		return (UnifiedOrderResponseData) xStream.fromXML(responseString);
	}
	
	/**
	 * 把XML字符串转换为统一下单回调接口请求数据对象
	 * @param requestString
	 * @return
	 * return:UnifiedOrderNotifyRequestData
	 */
	public static UnifiedOrderNotifyRequestData castXMLStringToUnifiedOrderNotifyRequestData(String requestString){
		XStream xStream = new XStream(new DomDriver());
		xStream.alias("xml", UnifiedOrderNotifyRequestData.class);
		return (UnifiedOrderNotifyRequestData) xStream.fromXML(requestString);
	}
	
	/**
	 * 把XML字符串转换为订单查询接口返回数据对象
	 * @param responseString
	 * @return
	 * return:OrderQueryResponseData
	 */
	public static OrderQueryResponseData castXMLStringToOrderQueryResponseData(String responseString){
		XStream xStream = new XStream(new DomDriver());
		xStream.alias("xml", OrderQueryResponseData.class);
		return (OrderQueryResponseData) xStream.fromXML(responseString);
	}
	
	/**
	 * 利用反射获取Java对象的字段并进行加密，过滤掉sign字段
	 * @param data
	 * @return
	 * return:String
	 */
	public static String getSign(Object data) {
		StringBuilder stringA = new StringBuilder();
		Map<String, String> map = new HashMap<String, String>();
		Field[] fields = data.getClass().getDeclaredFields();
		Method[] methods = data.getClass().getDeclaredMethods();
		for (Field field : fields) {
			String fieldName = field.getName();
			if (field != null && !fieldName.equals("sign")) {
				String getMethodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
				for (Method method : methods) {
					if (method.getName().equals(getMethodName)) {
						try {
							if (method.invoke(data, new Object[]{}) != null && method.invoke(data, new Object[]{}).toString().length() != 0) {
								map.put(fieldName, method.invoke(data, new Object[]{}).toString());
							}
						} catch (IllegalAccessException | IllegalArgumentException
								| InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		List<Map.Entry<String,String>> mappingList = null; 
    	//通过ArrayList构造函数把map.entrySet()转换成list 
    	mappingList = new ArrayList<Map.Entry<String,String>>(map.entrySet()); 
    	//通过比较器实现比较排序 
    	Collections.sort(
			mappingList, 
			new Comparator<Map.Entry<String,String>>(){ 
				public int compare(Map.Entry<String,String> mapping1,Map.Entry<String,String> mapping2){ 
					return mapping1.getKey().compareTo(mapping2.getKey()); 
				} 
	  		}
    	);
		for (Map.Entry<String, String> mapping : mappingList) {
			stringA.append("&").append(mapping.getKey()).append("=").append(mapping.getValue());
		}
		String stringSignTemp = stringA.append("&key=").append(WeixinPayConfig.KEY).substring(1);
		return WeixinUtil.MD5(stringSignTemp).toUpperCase();
	}

}