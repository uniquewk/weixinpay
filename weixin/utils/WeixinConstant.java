package com.fs.module.weixin.utils;

/**
 * 
 * @author wangkai
 * @2016年6月2日 上午10:57:33
 * @desc:微信支付常量类
 */
public class WeixinConstant {
	
	public static String SUCCESS = "SUCCESS"; //成功return_code
	
	public static String FAIL = "FAIL";   //失败return_code
	
	public static String PRODUCT_BODY = "xx时长充值卡"; // 商品描述
	
	public static String FEBDA_PAY_BODY = "xx分答支付"; // 商品描述
	
	public static String TRADE_TYPE = "JSAPI";//支付类型 JSAPI NATIVE(扫码支付) APP等等
	
	public static String SIGN_TYPE = "MD5";//签名类型
	
	public static String EPAY_DESC = "xxxx";//企业付款描述
}
