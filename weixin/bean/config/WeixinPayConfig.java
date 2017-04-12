package com.fs.module.weixin.bean.config;

/**
 * 
 * @author wangkai
 * @2016年7月4日 下午4:51:58
 * @desc:微信公众号支付配置信息
 */
public class WeixinPayConfig {
	// 公众号appid
	public final static String APPID = "xxx"; //
	// 公众号appsecret
	public final static String APPSECRET = "xxx";
	// 证书
	public static String CERT_FILE = System.getProperty("user.dir")
			+ System.getProperty("file.separator")+"fscert"+System.getProperty("file.separator")+"apiclient_cert.p12";//鱼说微信企业支付证书
	// 公众平台商户ID
	public final static String MCHID = "xxx"; //
	// 公众平台商户KEY
	public final static String KEY = "xxxx";
	// 微信商户平台支付结果通知URL
	// 线上环境 
    public final static String NOTIFY_URL = "https://pay.xxxxx.com/weixinpay/jsapi/callback/pay.action";
	// 统一下单URL
	public final static String WECHAT_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	// 查询订单URL
	public final static String WECHAT_ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";

	// 获取token接口(GET)
	public final static String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
	// ticket 接口 (GET)
	public final static String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
	// oauth2授权接口(GET)
	public final static String OAUTH2_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
}