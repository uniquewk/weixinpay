package com.fs.module.weixin.utils;
/**
 * 
 * @author ChaoChao
 *
 */
public enum PayErrorCodeMessage{
	/**
	 * 微信支付
	 */
	ERROR_SIGN(60001,"error sign"),
	RESULT_CODE_FAIL(60002,"result_code is fail"),
	RETURN_CODE_FAIL(60003,"return_code is fail"),
	TRADE_STATE_FAIL(60004,"trade_state is fail"),
	TRADE_STATE_REFUND(60005,"转入退款"),
	TRADE_STATE_NOTPAY(60006,"未支付"),
	TRADE_STATE_CLOSED(60007,"支付已关闭"),
	TRADE_STATE_REVOKED(60008,"已撤销（刷卡支付）"),
	TRADE_STATE_USERPAYING(60009,"用户支付中"),
	TRADE_STATE_PAYERROR(60010,"支付失败"),
	TRADE_STATE_UNKOWN(60011,"未知的失败状态");
	
	private int index;
	private String name;
	
	private PayErrorCodeMessage(int index, String name) {
		this.setIndex(index);
		this.setName(name);
	}

	public int getIndex() {
		return index;
	}

	private void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}
	
}
