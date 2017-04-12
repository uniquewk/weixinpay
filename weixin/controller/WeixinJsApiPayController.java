package com.fs.module.weixin.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.fs.common.HttpResult;
import com.fs.common.service.controller.AbstrackController;
import com.fs.module.weixin.bean.OrderQueryRequestData;
import com.fs.module.weixin.bean.UnifiedOrderNotifyRequestData;
import com.fs.module.weixin.bean.XmlData;
import com.fs.module.weixin.bean.util.WxPayUtil;
import com.fs.module.weixin.logic.WeixinJsPayLogic;
import com.fs.module.weixin.utils.ConceStrUtils;
import com.fs.module.weixin.utils.PayCommonUtil;
import com.fs.module.weixin.utils.WeixinConstant;
import com.fs.service.api.user.po.UserBaseInfoPo;
import com.fs.util.AuthUtil;
import com.fs.util.ResponseUtil;
import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * 
 * @author kevin
 * @2016年6月2日 下午1:24:02
 * @desc: JSAPI微信支付服务端入口
 */
@Path(value = "weixinpay")
public class WeixinJsApiPayController extends AbstrackController {

	@Inject
	private WeixinJsPayLogic weixinJsPayLogic;

	private static Logger logger = LoggerFactory.getLogger(WeixinJsApiPayController.class);

	/**
	 * 登陆授权
	 * 
	 * @param code
	 * @return
	 */
	@POST
	@Path(value = "/jsapi/oauth2")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response preOrder(@FormParam("code") final String code) {
		logger.debug("开始登陆授权...");
		logger.info("公众号支付,登陆授权: " + code);
		if (Strings.isNullOrEmpty(code)) {
			return ResponseUtil.toResponse(HttpResult.error(420, "code不能为空"));
		}
		// 通过code获取用户openid
		HttpResult<String> result = weixinJsPayLogic.authorize(code);
		logger.debug("结束登陆授权...");
		return Response.status(200).entity(result).build();
	}

	/**
	 * 统一下单
	 * 
	 * @param ip
	 *            用户公网ip
	 * @param authorization
	 *            cookie
	 * @param proId
	 *            商户订单号
	 * @param price
	 *            充值金额
	 * @param openid
	 *            用户唯一标识
	 * @return
	 */
	@POST
	@Path(value = "jsapi/preOrder")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response preOrder(
			@HeaderParam(value = "X-Forwarded-For") final String ip,
			@CookieParam("Authorization") String token,
			@FormParam("product_id") final String proId,// 商品id （订单号）
			@FormParam("price") final int price,// 支付money
			//@FormParam("openid") final String openid,// 回调code 用户获取openid
			@FormParam("type") int type// 1 支付充值 2 分答支付
	) {
		logger.debug("开始jsapi下单方法...");
		logger.info("weixin_pay unifiedOrder: " + token + "-" + proId);
		UserBaseInfoPo user = new AuthUtil().getUserStatusFrom(token);
		if (user == null || StringUtils.isEmpty(user.getUserId())) {
			return Response.status(200).entity("not login").build();
		}
		// 调用统一下单接口
		HttpResult<Object> result = weixinJsPayLogic.receipt(user.getUserId(), proId, price,
				ip,type);
		logger.debug("结束jsapi下单方法...");
		return Response.status(200).entity(result).build();
	}

	/**
	 * 
	 * 接收微信服务器支付结果回调
	 * 
	 * @param request
	 * @param response
	 * return:void
	 */
	@POST
	@Path(value = "jsapi/callback/pay.action")
	public Response callbackResult(@Context HttpRequest request) {
		logger.debug("开始接收支付结果通知（微信回调）...");
		//1、获取回调数据
		String result = "";
		BufferedReader br = null;
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			while((line = br.readLine())!=null){
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = PayCommonUtil.setXML(WeixinConstant.FAIL, "invalid data block");
		} finally{
			try {
				if(br != null){
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("received string in notifyRequest => " + sb.toString());
		//2、把接收到的数据转换成UnifiedOrderNotifyRequestData对象
		UnifiedOrderNotifyRequestData payResult = WxPayUtil.castXMLStringToUnifiedOrderNotifyRequestData(sb.toString());
		result = weixinJsPayLogic.getCallbackResponseData(payResult);
		logger.debug("结束支付结果通知（微信回调）...");
		return Response.status(200).entity(result).build();
	}

	/**
	 * 查询订单接口
	 * 
	 * @param outTradeNo
	 *            本地商户订单号
	 * @param transactionId
	 *            微信订单号
	 * @return 订单交易信息－充值信息
	 */
	@GET
	@Path(value = "/jsapi/queryOrder")
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryOrder(
			@QueryParam("out_trade_no") final String outTradeNo,
			@QueryParam("transaction_id") final String transactionId) {
		logger.debug("开始调用微信订单查询接口...");
		// 组织查询订单的请求数据对象
		OrderQueryRequestData orderQueryRequestData = new OrderQueryRequestData();
		// 优先使用out_trade_no进行订单查询 二选一 微信订单号优先
		if (Strings.isNullOrEmpty(outTradeNo)) {
			orderQueryRequestData.setTransaction_id(transactionId);
		} else {
			orderQueryRequestData.setOut_trade_no(outTradeNo);
		}
		orderQueryRequestData.setNonce_str(ConceStrUtils.createConceStr());
		orderQueryRequestData.setSign(WxPayUtil.getSign(orderQueryRequestData));

		logger.debug("orderQueryRequestData => "
				+ JSONObject.toJSONString(orderQueryRequestData));

		// 调用查询订单接口
		HttpResult<Object> result = weixinJsPayLogic
				.checkOrderStatus(orderQueryRequestData);
		logger.debug("结束调用微信订单查询接口...");
		return Response.status(200).entity(result).build();
	}

	/**
	 * 测试xml格式数据传输 使用resteasy xml -> bean 需要resteasy-jaxb-provider-2.2.0.GA.jar
	 * 进行处理
	 * 
	 * @param data
	 * @return
	 */
	@POST
	@Path(value = "/consume")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response testXmlConsume(XmlData data) {
		return Response.status(200).entity(data).build();
	}

	@GET
	@Path(value = "/produce")
	@Produces({ MediaType.APPLICATION_XML })
	public Response testXmlProduce() {
		XmlData data = new XmlData();
		data.setReturn_code("123");
		data.setReturn_msg("sadf");
		return Response.status(200).entity(data).build();

	}
}
