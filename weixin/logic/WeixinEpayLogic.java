package com.fs.module.weixin.logic;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import com.fs.common.HttpResult;
import com.fs.common.service.annotation.AddGuice;
import com.fs.common.utils.LoggerUtils;
import com.fs.module.weixin.bean.config.WeixinPayConfig;
import com.fs.module.weixin.utils.ConfigUtil;
import com.fs.module.weixin.utils.FsClientWithCertSSL;
import com.fs.module.weixin.utils.MapUtils;
import com.fs.module.weixin.utils.PayCommonUtil;
import com.fs.module.weixin.utils.PayErrorCodeMessage;
import com.fs.module.weixin.utils.WeixinConstant;
import com.fs.module.weixin.utils.XMLUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author wangkai
 * @2016年7月20日 下午7:59:29
 * @desc:微信企业支付逻辑
 */
@AddGuice
public class WeixinEpayLogic {

	/**
	 * 企业付款逻辑
	 *  
	 * @param user
	 * @param openId
	 *        微信openid
	 * @param ip
	 *        客户端ip地址
	 * @param amount
	 *        金额 单位：分
	 * @return
	 */
	public HttpResult<Map<String,Object>> epay(String openId,
			String ip,String amount,String tradeNo) {
		
		if(Strings.isNullOrEmpty(tradeNo) || Strings.isNullOrEmpty(openId)){
			LoggerUtils.settleLogger.error("企业支付开始=> openid: " +openId + "  tradeNo: " +tradeNo);
			return HttpResult.error(PayErrorCodeMessage.NO_PARAMS);
		}
		int amountInt = Integer.valueOf(amount).intValue();
		if(amountInt < 100|| Strings.isNullOrEmpty(amount)){
			LoggerUtils.settleLogger.error("企业支付开始=> 金额不得少于100分(1元)");
			return HttpResult.error(PayErrorCodeMessage.AMOUNT_LESS);
		}
		try {
			LoggerUtils.settleLogger.info("企业支付开始=>用户:"+openId+"  订单号:"+tradeNo+" 金额:"+amount);
			if(Strings.isNullOrEmpty(ip)){
			 InetAddress addr = InetAddress.getLocalHost();
	          ip = addr.getHostAddress().toString();
			}
			// 设置支付参数
			SortedMap<String, Object> parameters = getSignParams(tradeNo,openId,amountInt,ip);
			parameters.put("sign",PayCommonUtil.createSignPublic(Charsets.UTF_8.toString(), parameters));// sign签名
			String requestXML = PayCommonUtil.getRequestXml(parameters);// 生成xml格式字符串
			String responseStr = FsClientWithCertSSL.doPost(ConfigUtil.PROMOTION_URL, requestXML);
			// 解析结果
			Map<String, Object> resutlMap = XMLUtil.doXMLParse(responseStr);
			LoggerUtils.settleLogger.info("企业付款结果=> "+resutlMap.toString());
			if (resutlMap != null
					&& WeixinConstant.FAIL.equals(resutlMap.get("return_code"))) {
				LoggerUtils.settleLogger.error("企业付款失败 : " + tradeNo + "  失败原因:"
						+ resutlMap.get("return_msg"));
				return HttpResult.error(PayErrorCodeMessage.TRADE_STATE_PAYERROR);
			}
			if (WeixinConstant.SUCCESS.equalsIgnoreCase(resutlMap.get("result_code")
					.toString())) {
				Map<String, Object> map = buildClientJson(resutlMap);
				LoggerUtils.settleLogger.info("企业付款成功 :"+map.toString());
				return HttpResult.success(0, map);
			}
		} catch (Exception e) {
			LoggerUtils.settleLogger.error("企业付款失败{},{} ",openId,e.getMessage());
			return HttpResult.error(PayErrorCodeMessage.TRADE_STATE_PAYERROR);
		}
		return HttpResult.error(PayErrorCodeMessage.TRADE_STATE_PAYERROR);
	}
    
	/**
	 * 组装响应数据
	 * @param resutlMap
	 *        付款响应结果
	 * @return
	 */
	private Map<String, Object> buildClientJson(Map<String, Object> resutlMap) {
		
		if (resutlMap == null || resutlMap.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Object> returnMap = ImmutableMap.<String, Object> builder()
				.put("trade_no", resutlMap.get("partner_trade_no"))
				.put("payment_no", resutlMap.get("payment_no"))
				.put("payment_time", resutlMap.get("payment_time"))
				.put("result_msg", resutlMap.get("result_code"))
				.build();
		return returnMap;
	}

	/**
	 * 组合sign签名参数
	 * 
	 * @return
	 */
	private SortedMap<String, Object> getSignParams(String tradeNo,
			String openId, int amount, String ip) {
		Map<String, Object> oparams = ImmutableMap
				.<String, Object> builder()
				.put("mch_appid", WeixinPayConfig.APPID)
				// 服务号的应用号
				.put("desc", WeixinConstant.EPAY_DESC)
				// 企业付款描述信息
				.put("mchid", WeixinPayConfig.MCHID)
				// 商户号
				.put("nonce_str", PayCommonUtil.CreateNoncestr())
				// 16随机字符串(大小写字母加数字)
				.put("device_info",
						PayCommonUtil.createConceStr(32).toUpperCase())// 设备号 暂时写死
				.put("partner_trade_no", tradeNo)// 商户订单号
				.put("openid", openId)// 用户openid 注意:微信的openid
				.put("check_name", "NO_CHECK")// 不校验真实姓名 看后期情况
				.put("amount", amount)// 金额
				.put("spbill_create_ip", ip)// ip地址
				.build();
		return MapUtils.sortMap(oparams);
	}
    
	/**
	 * 企业付款查询
	 * @param outTradeNo
	 * @return
	 */
	public HttpResult<Map<String,Object>> epayQuery(String outTradeNo) {
		if(Strings.isNullOrEmpty(outTradeNo)){
			return HttpResult.error(PayErrorCodeMessage.NO_PARAMS);
		}
		try {
			//组装查询参数
			SortedMap<String, Object> params = buildQueryParams(outTradeNo);
			String requestXML = PayCommonUtil.getRequestXml(params);// 生成xml格式字符串
			// 带上post请求支付查询接口
			String responseStr = FsClientWithCertSSL.doPost(ConfigUtil.PROMOTION_QUERY_URL, requestXML);
			SortedMap<String, Object> responseMap = XMLUtil
					.doXMLParse(responseStr);// 解析响应xml格式字符串
			// 校验响应结果return_code
			if (WeixinConstant.FAIL.equalsIgnoreCase(responseMap.get(
					"return_code").toString())) {
				return HttpResult.error(1, responseMap.get("return_msg")
						.toString());
			}
			// 校验业务结果result_code
			if (WeixinConstant.FAIL.equalsIgnoreCase(responseMap.get(
					"result_code").toString())) {
				return HttpResult.error(2, responseMap.get("err_code")
						.toString() + "=" + responseMap.get("err_code_des"));
			}
			// 组装响应数据
		    Map<String,Object> resultMap = buildResponse(responseMap);
		    LoggerUtils.payLogger.info("企业付款查询=> "+ resultMap);
			return HttpResult.success(0, resultMap);
		} catch (Exception e) {
			LoggerUtils.settleLogger.error("付款查询异常{}{}",outTradeNo,e.getMessage());
			return HttpResult.error(PayErrorCodeMessage.QUERY_EXCEPTION);
		}
	}
	
	/**
	 * 封装查询结果数据
	 * @param responseMap
	 *        查询结果
	 * @return
	 */
	private Map<String, Object> buildResponse(
			SortedMap<String, Object> responseMap) {
		Map<String, Object> returnMap = ImmutableMap.<String, Object> builder()
				.put("trade_no", responseMap.get("partner_trade_no"))
				.put("payment_no", responseMap.get("detail_id"))
				.put("payment_account", responseMap.get("payment_amount"))
				.put("transfer_time", responseMap.get("transfer_time"))
				.put("result_code",responseMap.get("result_code"))
				.build();
		return returnMap;
	}

	/**
     * 组装查询参数
     * @param outTradeNo
     * @return
     */
	private SortedMap<String, Object> buildQueryParams(String outTradeNo) {
		// 组装查询参数- 可以使用treemap
		Map<String, Object> queryParams = ImmutableMap
				.<String, Object> builder()
				.put("appid", WeixinPayConfig.APPID)// 商户号的appid
				.put("mch_id", WeixinPayConfig.MCHID)// 商户号
				.put("nonce_str", PayCommonUtil.CreateNoncestr())// 16随机字符串(大小写字母加数字)
				.put("partner_trade_no", outTradeNo)// 商户订单号
				.build();
		// key ASCII 排序
		SortedMap<String, Object> sortMap = MapUtils.sortMap(queryParams);
		// MD5签名
		String createSign = PayCommonUtil.createSignPublic(Charsets.UTF_8.toString(), sortMap);
		sortMap.put("sign", createSign);
		return sortMap;
	}
	
	// 测试入口
	public static void main(String[] args) {
		WeixinEpayLogic logic = new WeixinEpayLogic(); //oditPuIgOzhtiO_kDFbiU8chhb-I  oS32Wt5t_rpSmzif-S6RrGsy6G0k
		logic.epay("oditPuIgOzhtiO_kDFbiU8chhb-I", "192.168.4.198", "100", "213232xx1xxsf1123553"); // openid 微信的openid ，单笔最小金额1元
		logic.epayQuery("213232xx1xxsf1123553");
		
	}
	
	

}
