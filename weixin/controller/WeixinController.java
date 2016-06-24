package com.fs.module.weixin.controller;

import java.util.Map;
import java.util.SortedMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpRequest;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fs.common.HttpResult;
import com.fs.common.RestResult;
import com.fs.common.service.controller.AbstrackController;
import com.fs.module.weixin.logic.WeixinLogic;
import com.fs.module.weixin.utils.ConfigUtil;
import com.fs.module.weixin.utils.MapUtils;
import com.fs.module.weixin.utils.PayCommonUtil;
import com.fs.util.AuthUtil;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

/**
 * @author wangkai
 * @2016年6月1日 下午8:39:13
 * @desc:APP微信支付入口
 */
@Path(value = "/weixin/pay")
@Produces(MediaType.APPLICATION_JSON)
public class WeixinController extends AbstrackController {

	@Inject
	private WeixinLogic weixinLogic;

	/**
	 * 微信支付 统一下订单入口
	 * 
	 * @param ip
	 * @param authorization
	 * @param proId
	 * @param price
	 * @param time
	 * @return
	 */
	@POST
	@Path(value = "/preOrder")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unifiedOrder(
			@HeaderParam(value = "X-Forwarded-For") final String ip,
			@HeaderParam("Authorization") String authorization,
			@FormParam("product_id") final String proId,// 商品id
			@FormParam("price") final int price// 支付money
	) {
		logger.info("weixin_pay unifiedOrder: " + authorization + "-" + proId);
		String userId = AuthUtil.getUserIdFrom(authorization);
		if (StringUtils.isEmpty(userId)) {
			return Response.status(400).entity("not login").build();
		}
		RestResult result = weixinLogic.unifiedOrder(userId, proId,
				"192.168.3.66", price);
		return Response.status(200).entity(result).build();
	}

	/**
	 * 微信回调
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Path(value = "/callback/pay.action")
	public Response weiixinCall(@Context HttpRequest request) {
		logger.info("weixinpay callback 开始...");
		String str = weixinLogic.callback(request);
		logger.info("weixinpay callback 结束...");
		return Response.status(200).entity(str).build();
	}

	/**
	 * 查询订单接口 该接口提供所有微信支付订单的查询， 商户可以通过该接口主动查询订单状态， 完成下一步的业务逻辑。
	 * 
	 * @param outTradeNo
	 *            商户订单号
	 * @param transactionId
	 *            微信订单号
	 * @return
	 */
	@POST
	@Path(value = "/queryOrder")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryOrder(
			@HeaderParam("Authorization") String authorization,
			@FormParam("outTradeNo") final String outTradeNo,
			@FormParam("transactionId") final String transactionId) {
		logger.debug("开始调用微信订单查询接口...");
		// 组织查询订单的请求数据
		try {
			logger.info("weixin_pay unifiedOrder: " + authorization + "-"
					+ outTradeNo + "-" + transactionId);
			SortedMap<String, Object> params = prepareQueryData(outTradeNo,
					transactionId);
			logger.debug("查询订单的请求数据 ={}" + JSONObject.toJSONString(params));
			// 调用查询订单接口
			HttpResult<String> result = weixinLogic.checkOrderStatus(params);
			logger.debug("结束调用微信订单查询接口...");
			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			logger.error("结束调用微信订单查询接口...{} {}", outTradeNo + "-"
					+ transactionId, e.getMessage());
			RestResult result = RestResult.fail("查询失败");
			return Response.status(200).entity(result).build();
		}
	}

	/**
	 * 封装查询请求数据
	 * @param outTradeNo 
	 * @param transactionId
	 * @return
	 */
	private SortedMap<String, Object> prepareQueryData(String outTradeNo,
			String transactionId) {
		Map<String, Object> queryParams = null;
		// 微信的订单号，优先使用
		if (null == outTradeNo || outTradeNo.length() == 0) {
			queryParams = ImmutableMap.<String, Object> builder()
					.put("appid", ConfigUtil.APPID)
					.put("mch_id", ConfigUtil.MCH_ID)
					.put("transaction_id", transactionId)
					.put("nonce_str", PayCommonUtil.CreateNoncestr()).build();
		} else {
			queryParams = ImmutableMap.<String, Object> builder()
					.put("appid", ConfigUtil.APPID)
					.put("mch_id", ConfigUtil.MCH_ID)
					.put("out_trade_no", outTradeNo)
					.put("nonce_str", PayCommonUtil.CreateNoncestr()).build();
		}
		// key ASCII 排序
		SortedMap<String, Object> sortMap = MapUtils.sortMap(queryParams);
		// 签名
		String createSign = PayCommonUtil.createSign("UTF-8", sortMap);
		sortMap.put("sign", createSign);
		return sortMap;
	}

}