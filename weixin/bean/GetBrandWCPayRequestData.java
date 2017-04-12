package com.fs.module.weixin.bean;
/**
 * 在微信浏览器里面打开H5网页中执行JS调起支付。接口输入输出数据格式为JSON。
 * 注意：WeixinJSBridge内置对象在其他浏览器中无效；列表中参数名区分大小。
 * @type_name:GetBrandWCPayRequestData
 * @time:下午2:37:13
 * @author:Frankhbu
 */
public class GetBrandWCPayRequestData {

	//appId 是 String(16) wx8888888888888888 商户注册具有支付权限的公众号成功后即可获得 
	String appId;
	//时间戳 timeStamp 是 String(32) 1414561699 当前的时间，其他详见时间戳规则 
	String timeStamp;
	//随机字符串 nonceStr 是 String(32) 5K8264ILTKCH16CQ2502SI8ZNMTM67VS 随机字符串，不长于32位。推荐随机数生成算法 
	String nonceStr;
	//订单详情扩展字符串 package 是 String(128) prepay_id=123456789 统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=*** 
	String packageStr;
	//签名方式 signType 是 String(32) MD5 签名算法，暂支持MD5 
	String signType;
	//签名 paySign 是 String(64) C380BEC2BFD727A4B6845133519F3AD6 签名，详见签名生成算法 
	String paySign;
	
	public GetBrandWCPayRequestData(String appId, String timeStamp,
			String nonceStr, String packageStr, String signType) {
		super();
		this.appId = appId;
		this.timeStamp = timeStamp;
		this.nonceStr = nonceStr;
		this.packageStr = packageStr;
		this.signType = signType;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getPackageStr() {
		return packageStr;
	}
	public void setPackageStr(String packageStr) {
		this.packageStr = packageStr;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getPaySign() {
		return paySign;
	}
	public void setPaySign(String paySign) {
		this.paySign = paySign;
	}
}