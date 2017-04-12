package com.fs.module.weixin.bean;

import com.fs.module.weixin.bean.config.WeixinPayConfig;

/**
 * 该接口提供所有微信支付订单的查询，商户可以通过该接口主动查询订单状态，完成下一步的业务逻辑。
 * 需要调用查询接口的情况：
 *	◆ 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；
 *  ◆ 调用支付接口后，返回系统错误或未知交易状态情况；
 *  ◆ 调用被扫支付API，返回USERPAYING的状态；
 *  ◆ 调用关单或撤销接口API之前，需确认支付状态；
 */
public class OrderQueryRequestData {
	
	//公众账号ID	是	String(32)	wx8888888888888888	微信分配的公众账号ID（企业号corpid即为此appId）
	private String appid = WeixinPayConfig.APPID;
	//商户号	是	String(32)	1900000109	微信支付分配的商户号
	private String mch_id = WeixinPayConfig.MCHID;
	//微信订单号	否	String(32)	013467007045764	微信的订单号，优先使用
	private String transaction_id;
	//商户订单号	否	String(32)	1217752501201407033233368018	商户系统内部的订单号，当没提供transaction_id时需要传这个。
	private String out_trade_no;
	//随机字符串	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
	private String nonce_str;
	//签名	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	签名，详见签名生成算法
	private String sign;
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	
}