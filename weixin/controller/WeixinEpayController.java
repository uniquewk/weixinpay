package com.fs.module.weixin.controller;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fs.common.HttpResult;
import com.fs.common.service.controller.AbstrackController;
import com.fs.module.weixin.logic.WeixinEpayLogic;
import com.fs.module.weixin.utils.PayErrorCodeMessage;
import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * 
 * @author wangkai
 * @2016年7月20日 下午8:04:51
 * @desc:微信企业付款入口
 */
@Path("weixinepay")
public class WeixinEpayController extends AbstrackController {

	@Inject
	private WeixinEpayLogic epayLogic; // 企业付款逻辑

	/**
	 * 企业支付入口
	 * 
	 * @param ip
	 * @param token
	 * @param cookie
	 * @param openId
	 * @param amount
	 * @param tradeNo
	 * @return
	 */
	@POST
	@Path(value = "/enterprisePay")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unifiedOrder(
			//TODO 区分公众号和其它应用(app and web)
			//@HeaderParam(value = "X-Forwarded-For") final String ip,
			//@HeaderParam("Authorization") String token, // token
			//@CookieParam("Authorization") String cookie, // cookie
			@FormParam("ip") final String ip,//IP 地址
			@FormParam("openid") final String openId,// 用户openid
			@FormParam("amount") final String amount,// 支付金额 单位分
			@FormParam("trade_no") String tradeNo// 商户订单号
	) {
		logger.info("企业付款: ip" + ip + " 用户：" + openId + " 金额：" +amount+ " 订单号:"
				+ tradeNo);
		/*UserBaseInfoPo user = null;
		if (!Strings.isNullOrEmpty(token)) {
			user = AuthUtil.getUserIdFrom(token);
		} else if (Strings.isNullOrEmpty(cookie)) {
			user = AuthUtil.getUserIdFrom(cookie);
		}
		if (Strings.isNullOrEmpty(user.getUserId())) {
			return Response.status(400).entity("not login").build();
		}*/
		HttpResult<Map<String, Object>> result = epayLogic.epay(openId,
				ip, amount, tradeNo);
		return Response.status(200).entity(result).build();
	}

	/**
	 * 企业付款查询
	 * 
	 * @param authorization
	 * @param outTradeNo
	 *            商户订单号
	 * @param transactionId
	 *            微信订单号
	 * @return
	 */
	@GET
	@Path(value = "/entrepriseQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response epayQuery(
			//@HeaderParam("Authorization") String authorization,
			@QueryParam("trade_no") final String outTradeNo,// 商户订单号
			@QueryParam("transaction_id") final String transactionId// 微信订单号
	) {
		logger.debug("开始调用微信企业支付查询接口...");
		// 组织查询订单的请求数据
		try {
			logger.info("enterprise pay query : "
					+ outTradeNo + "-" + transactionId);
			if (Strings.isNullOrEmpty(outTradeNo)) {
				HttpResult<Object> result = HttpResult.error(
						PayErrorCodeMessage.NO_PARAMS.getIndex(),
						PayErrorCodeMessage.NO_PARAMS.getName());
				return Response.status(200).entity(result).build();
			}
			HttpResult<Map<String, Object>> result = epayLogic
					.epayQuery(outTradeNo);
			logger.debug("结束调用微信支付查询接口...");
			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			logger.error("结束调用微企业支付单查询接口{} {}", outTradeNo + "-"
					+ transactionId, e.getMessage());
			HttpResult<Object> eresult = HttpResult.error(PayErrorCodeMessage.QUERY_EXCEPTION);
			return Response.status(200).entity(eresult).build();
		}
	}
}
