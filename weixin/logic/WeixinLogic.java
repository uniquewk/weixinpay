package com.fs.module.weixin.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.fs.PayApp;
import com.fs.common.HttpResult;
import com.fs.common.RestResult;
import com.fs.common.service.AbstractApp;
import com.fs.module.weixin.utils.ConfigUtil;
import com.fs.module.weixin.utils.DateUtils;
import com.fs.module.weixin.utils.HttpUtil;
import com.fs.module.weixin.utils.MapUtils;
import com.fs.module.weixin.utils.PayCommonUtil;
import com.fs.module.weixin.utils.WeixinConstant;
import com.fs.module.weixin.utils.XMLUtil;
import com.fs.service.api.pay.IPayService;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * 
 * @author wangkai
 * @2016年6月1日 下午8:55:36
 * @desc：微信支付逻辑(统一下单,微信回调,订单查询)
 */
public class WeixinLogic {

	private static final Logger logger = LoggerFactory
			.getLogger(WeixinLogic.class);

	/**
	 * 微信预支付 统一下单入口
	 * 
	 * @author wangkai
	 * @param userId
	 *            用户id
	 * @param proId
	 *            商品id
	 * @return
	 */
	public RestResult unifiedOrder(String userId, String proId, String ip,
			int price) {
		try {
			logger.info("统一下定单开始");
			// 设置订单参数,
			IPayService payService = (IPayService) AbstractApp
					.getRpcService("payService");
			String orderId = payService.weixinPayLog(userId, proId);// 获取订单id
			// 设置订单参数
			SortedMap<String, Object> parameters = prepareOrder(ip, orderId,
					price);
			parameters.put("sign",
					PayCommonUtil.createSign("UTF-8", parameters));// sign签名 key
			String requestXML = PayCommonUtil.getRequestXml(parameters);// 生成xml格式字符串
			String responseStr = HttpUtil.httpsRequest(
					ConfigUtil.UNIFIED_ORDER_URL, "POST", requestXML);// 带上post
			// 检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改
			if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
				logger.error("微信统一下单失败,签名可能被篡改");
				return RestResult.fail("统一下单失败");
			}
			// 解析结果 resultStr
			SortedMap<String, Object> resutlMap = XMLUtil
					.doXMLParse(responseStr);
			if (resutlMap != null
					&& WeixinConstant.FAIL.equals(resutlMap.get("return_code"))) {
				logger.error("微信统一下单失败,订单编号:" + orderId + ",失败原因:"
						+ resutlMap.get("return_msg"));
				return RestResult.fail("统一下单失败");
			}
			// 获取到 prepayid
			// 商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易回话标识后再在APP里面调起支付。
			SortedMap<String, Object> map = buildClientJson(resutlMap);
			map.put("outTradeNo", orderId);
			logger.info("统一下定单结束");
			return RestResult.OK(map);
		} catch (Exception e) {
			logger.error(
					"com.fs.module.weixin.logic.WeixinLogic receipt(String userId,String proId,String ip)：{},{}",
					userId + "-" + proId + "-" + ip, e.getMessage());
			return RestResult.fail("预支付请求失败"); // 抽离到统一错误码泪中 统一定一下
		}

	}

	/**
	 * 生成订单信息
	 * 
	 * @param ip
	 * @param orderId
	 * @return
	 */
	private SortedMap<String, Object> prepareOrder(String ip, String orderId,
			int price) {
		Map<String, Object> oparams = ImmutableMap.<String, Object> builder()
				.put("appid", ConfigUtil.APPID)// 服务号的应用号
				.put("body", WeixinConstant.PRODUCT_BODY)// 商品描述
				.put("mch_id", ConfigUtil.MCH_ID)// 商户号 ？
				.put("nonce_str", PayCommonUtil.CreateNoncestr())// 16随机字符串(大小写字母加数字)
				.put("out_trade_no", orderId)// 商户订单号
				.put("total_fee", "1")// 银行币种 price
				.put("spbill_create_ip", ip)// IP地址
				.put("notify_url", ConfigUtil.NOTIFY_URL) // 微信回调地址
				.put("trade_type", ConfigUtil.TRADE_TYPE)// 支付类型 app
				.build();
		return MapUtils.sortMap(oparams);
	}

	/**
	 * 生成预付快订单完成，返回给android,ios唤起微信所需要的参数。
	 * 
	 * @param resutlMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private SortedMap<String, Object> buildClientJson(
			Map<String, Object> resutlMap) throws UnsupportedEncodingException {
		// 获取微信返回的签名

		/**
		 * backObject.put("appid", appid);
		 * 
		 * backObject.put("noncestr", payParams.get("noncestr"));
		 * 
		 * backObject.put("package", "Sign=WXPay");
		 * 
		 * backObject.put("partnerid", payParams.get("partnerid"));
		 * 
		 * backObject.put("prepayid", payParams.get("prepayid"));
		 * 
		 * backObject.put("appkey", this.appkey);
		 * 
		 * backObject.put("timestamp",payParams.get("timestamp"));
		 * 
		 * backObject.put("sign",payParams.get("sign"));
		 */
		Map<String, Object> params = ImmutableMap.<String, Object> builder()
				.put("appid", ConfigUtil.APPID)
				.put("noncestr", PayCommonUtil.CreateNoncestr())
				.put("package", "Sign=WXPay")
				.put("partnerid", ConfigUtil.MCH_ID)
				.put("prepayid", resutlMap.get("prepay_id"))
				.put("timestamp", DateUtils.getTimeStamp()).build();
		// key ASCII排序
		SortedMap<String, Object> sortMap = MapUtils.sortMap(params);
		sortMap.put("package", "Sign=WXPay");
		// paySign的生成规则和Sign的生成规则同理
		String paySign = PayCommonUtil.createSign("UTF-8", sortMap);
		sortMap.put("sign", paySign);
		return sortMap;
	}

	/**
	 * 微信回调告诉微信支付结果 注意：同样的通知可能会多次发送给此接口，注意处理重复的通知。
	 * 对于支付结果通知的内容做签名验证，防止数据泄漏导致出现“假通知”，造成资金损失。
	 * 
	 * @param params
	 * @return
	 */
	public String callback(HttpRequest request) {
		try {
			String responseStr = parseWeixinCallback(request);
			Map<String, Object> map = XMLUtil.doXMLParse(responseStr);
			// 校验签名 防止数据泄漏导致出现“假通知”，造成资金损失
			if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
				logger.error("微信回调失败,签名可能被篡改");
				return PayCommonUtil.setXML("FAIL", "invalid sign");
			}
			if (WeixinConstant.FAIL.equalsIgnoreCase(map.get("result_code")
					.toString())) {
				logger.error("微信回调失败");
				return PayCommonUtil.setXML("FAIL", "weixin pay fail");
			}
			if (WeixinConstant.SUCCESS.equalsIgnoreCase(map.get("result_code")
					.toString())) {
				// 对数据库的操作
				String outTradeNo = (String) map.get("out_trade_no");
				String transactionId = (String) map.get("transaction_id");
				String totlaFee = (String) map.get("total_fee");
				Integer totalPrice = Integer.valueOf(totlaFee);
				if (PayApp.theApp.isDebug()) {// 测试时候支付一分钱，买入价值6块的20分钟语音
					totalPrice = 6;
				}
				boolean isOk = updateDB(outTradeNo, transactionId, totalPrice,
						2);
				// 告诉微信服务器，我收到信息了，不要在调用回调action了
				if (isOk) {
					return PayCommonUtil.setXML(WeixinConstant.SUCCESS, "OK");
				} else {
					return PayCommonUtil
							.setXML(WeixinConstant.FAIL, "pay fail");
				}
			}
		} catch (Exception e) {
			logger.debug("支付失败" + e.getMessage());
			return PayCommonUtil.setXML(WeixinConstant.FAIL,
					"weixin pay server exception");
		}
		return PayCommonUtil.setXML(WeixinConstant.FAIL, "weixin pay fail");
	}

	/**
	 * 解析微信回调参数
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private String parseWeixinCallback(HttpRequest request) throws IOException {
		InputStream inStream = request.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		String result = new String(outSteam.toByteArray(), "utf-8");// 获取微信调用我们notify_url的返回信息
		return result;
	}

	/**
	 * 查询订单状态
	 * 
	 * @param params
	 *            订单查询参数
	 * @return
	 */
	public HttpResult<String> checkOrderStatus(SortedMap<String, Object> params) {
		if (params == null) {
			return HttpResult.error(1, "查询订单参数不能为空");
		}
		try {
			String requestXML = PayCommonUtil.getRequestXml(params);// 生成xml格式字符串
			String responseStr = HttpUtil.httpsRequest(
					ConfigUtil.CHECK_ORDER_URL, "POST", requestXML);// 带上post
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
			// 校验签名
			if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
				logger.error("订单查询失败,签名可能被篡改");
				return HttpResult.error(3, "签名错误");
			}
			// 判断支付状态
			String tradeState = responseMap.get("trade_state").toString();
			if (tradeState != null && tradeState.equals("SUCCESS")) {
				return HttpResult.success(0, "订单支付成功");
			} else if (tradeState == null) {
				return HttpResult.error(4, "获取订单状态失败");
			} else if (tradeState.equals("REFUND")) {
				return HttpResult.error(5, "转入退款");
			} else if (tradeState.equals("NOTPAY")) {
				return HttpResult.error(6, "未支付");
			} else if (tradeState.equals("CLOSED")) {
				return HttpResult.error(7, "已关闭");
			} else if (tradeState.equals("REVOKED")) {
				return HttpResult.error(8, "已撤销（刷卡支付");
			} else if (tradeState.equals("USERPAYING")) {
				return HttpResult.error(9, "用户支付中");
			} else if (tradeState.equals("PAYERROR")) {
				return HttpResult.error(10, "支付失败");
			} else {
				return HttpResult.error(11, "未知的失败状态");
			}
		} catch (Exception e) {
			logger.error("订单查询失败,查询参数 = {}", JSONObject.toJSONString(params));
			return HttpResult.success(1, "订单查询失败");
		}
	}
    
	/**
	 * 操作本地服务 支付数据持久化
	 * @param outTradeNo
	 * @param tradeNo
	 * @param price
	 * @param type
	 * @return
	 */
	private boolean updateDB(String outTradeNo, String tradeNo, int price,
			int type) {
		IPayService payService = (IPayService) AbstractApp
				.getRpcService("payService");
		return payService.alipay(outTradeNo, tradeNo, price, null, type);
	}
}
