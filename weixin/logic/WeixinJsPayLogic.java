package com.fs.module.weixin.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fs.PayApp;
import com.fs.common.HttpResult;
import com.fs.common.service.AbstractApp;
import com.fs.common.service.annotation.AddGuice;
import com.fs.common.utils.LoggerUtils;
import com.fs.message.mns.producer.MessageSender;
import com.fs.module.weixin.bean.GetBrandWCPayRequestData;
import com.fs.module.weixin.bean.OrderQueryRequestData;
import com.fs.module.weixin.bean.OrderQueryResponseData;
import com.fs.module.weixin.bean.UnifiedOrderNotifyRequestData;
import com.fs.module.weixin.bean.UnifiedOrderRequestData;
import com.fs.module.weixin.bean.UnifiedOrderResponseData;
import com.fs.module.weixin.bean.config.WeixinPayConfig;
import com.fs.module.weixin.bean.util.WxPayUtil;
import com.fs.module.weixin.utils.ConceStrUtils;
import com.fs.module.weixin.utils.DateUtils;
import com.fs.module.weixin.utils.HttpClientUtil;
import com.fs.module.weixin.utils.MapUtils;
import com.fs.module.weixin.utils.PayCommonUtil;
import com.fs.module.weixin.utils.PayErrorCodeMessage;
import com.fs.module.weixin.utils.WeixinConstant;
import com.fs.service.api.fenda.IFendaService;
import com.fs.service.api.fenda.message.PayedMessage;
import com.fs.service.api.fenda.po.AppFendaCheckoutPo;
import com.fs.service.api.login.po.UserOauthPo;
import com.fs.service.api.pay.IPayService;
import com.fs.service.api.user.IUserService;
import com.fs.service.constant.mnsmessage.PayedMessaeTypeEnum;
import com.fs.service.constant.mnsmessage.QueueEnum;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author wangkai
 * @2016年7月5日 上午10:23:14
 * @desc:微信公众号支付
 */
@AddGuice
public class WeixinJsPayLogic {
	
	IUserService userService = (IUserService)AbstractApp.getRpcService("userService");
	IFendaService fendaService = (IFendaService) AbstractApp.getRpcService("fendaService");
	/**
	 * 微信预支付
	 * @param userId
	 * @param proId
	 * @param ip
	 * @return
	 */
	public HttpResult<Object> receipt(String userId, String proId,int price,String ip,int type) {
		//获取用户的openid
		UserOauthPo oatuhs = userService.findUserOauthByUserId(userId);
		//String openid = "oditPuIgOzhtiO_kDFbiU8chhb-I";//公众号openid
		//String openid = "oditPuC2DzLWjdc1eo1ipz4nx-0U";//关注公众号openid
		//String openid = "oS32Wt5t_rpSmzif-S6RrGsy6G0k";//微信openid
		
		String openid = "";
		if(oatuhs != null){
			openid = oatuhs.getOauthId();
		}
		// 可以改为占位符好些 kevin
		LoggerUtils.payLogger.info("jsapi支付下订单,用户："+userId+" 商品id："+proId+" ip: "+ip+" openid: "+openid+" 支付分类："+type +" 金额：" +price +"分");
		HttpResult<Object> result = null;
		try {
			if(StringUtils.isEmpty(openid)){
				result = HttpResult.error(420, "invalid arguments openid");
			}
			//调用统一下单接口
			//这里需要注意price 单位分
			UnifiedOrderResponseData responseData = unifiedOrder(userId, proId, price, ip,openid,type);
			//生成web前端可用数据
			result = getAppPackage(responseData);
		} catch (Exception e) {
			LoggerUtils.payLogger.error(
					"com.fs.module.weixin.logic.WeixinLogic "
					+ "receipt(String userId,String proId,String ip)：{},{}",
					"用户:"+userId + " 商品id" + proId + " ip" + ip, " 异常 "+e.getMessage());
			result = HttpResult.error(200, "invalid arguments");
		}
		return result;
	}
	
	/**
	 * 验证微信支付结果回调，并更新本地订单状态
	 * @param requestData
	 *        微信回调响应的数据
	 * @return
	 */
	public String getCallbackResponseData(
			UnifiedOrderNotifyRequestData requestData) {
		String responseData = null;
		if (null == requestData) {
			LoggerUtils.payLogger.error("微信支付回调参数有误");
			responseData =  PayCommonUtil.setXML(WeixinConstant.FAIL, "data is null or empty");
			return responseData;
		}
		LoggerUtils.payLogger.info("UnifiedOrderNotifyRequestData => "
				+ JSONObject.toJSONString(requestData));
		String responseSign = WxPayUtil.getSign(requestData);
		do {
			// 判断签名－以防在网络传输过程中被篡改
			if (responseSign == null
					|| !responseSign.equals(requestData.getSign())) {
				responseData = PayCommonUtil.setXML(WeixinConstant.FAIL, "invalid sign");
				break;
			}
			// 判断微信回调参数完整性
			// 本地商户订单号
			String outTradeNo = requestData.getOut_trade_no();
			// 微信订单号
			String transactionId = requestData.getTransaction_id();
			if (requestData.getReturn_code() == null
					|| requestData.getResult_code() == null
					|| requestData.getBank_type() == null
					|| requestData.getTotal_fee() == null
					|| Strings.isNullOrEmpty(outTradeNo)
					|| Strings.isNullOrEmpty(transactionId)) {
				responseData = PayCommonUtil.setXML(WeixinConstant.FAIL, "important param is null");
				break;
			}
			// 支付失败
			if (WeixinConstant.FAIL.equals(requestData.getResult_code())) {
				responseData = PayCommonUtil.setXML(WeixinConstant.FAIL, "weixin pay fail");
				break;
			}
			// 数据持久化操作 1充值支付 2 分答支付 
			int price = Integer.valueOf(requestData.getTotal_fee());
			if (PayApp.theApp.isDebug()) {// 测试时候支付一分钱，买入价值6块的20分钟语音
				price = 6;
			}
			// 更新本地订单信息
			boolean isOk = updateDB(outTradeNo, transactionId, price, 2);// 2微信支付标识
			if (isOk) {
			responseData = PayCommonUtil.setXML(WeixinConstant.SUCCESS, "OK");
			// 发送支付ok消息到mns
			// 通过订单号 区分支付类型－ 提问支付或者偷听支付
			AppFendaCheckoutPo po = fendaService.getFendaCheckoutById(outTradeNo);
			if(po != null ){
				PayedMessage payokm = new PayedMessage();// 消息体
				if(!Strings.isNullOrEmpty(po.getListenId())){
					// 发送支付ok消息到mns
					payokm.setTradeNo(outTradeNo);// 商户订单号
					payokm.setType(PayedMessaeTypeEnum.LISTEN_PAYED.getIndex());//偷听支付
				}else{
					// 发送支付ok消息到mns
					payokm.setTradeNo(outTradeNo);// 商户订单号
					payokm.setType(PayedMessaeTypeEnum.QUESTION_PAYED.getIndex());//分答提问支付
				}
				if(PayApp.theApp.isDebug()){
					MessageSender.send(payokm, "LocalFendaPayQueue");
				}else{
					MessageSender.send(payokm, QueueEnum.FENDA_PAY.getQueueName());//放入那个队列 //注意：这里发送消息是否考虑用异步操作
				}
			}
			break;
			} else {
				responseData = PayCommonUtil.setXML(WeixinConstant.FAIL, "update bussiness outTrade fail");
				break;
			}
		} while (false);
		return responseData;
	}
	
	/**
	 * 查询订单状态
	 * 
	 * @param orderQueryRequestData
	 * @return
	 */
	public HttpResult<Object> checkOrderStatus(
			OrderQueryRequestData orderQueryRequestData) {
		HttpResult<Object> result = null;
		// 调用查询微信订单接口
		OrderQueryResponseData orderQueryResponseData = WxPayUtil
				.queryOrder(orderQueryRequestData);
		LoggerUtils.payLogger.info("orderQueryResponseData => "
				+ JSONObject.toJSONString(orderQueryResponseData));
		do {
			// return_code
			if (orderQueryResponseData.getReturn_code() == null
					|| !orderQueryResponseData.getReturn_code().equals(
							"SUCCESS")) {
				result = HttpResult.success(
						PayErrorCodeMessage.RETURN_CODE_FAIL.getIndex(),
						orderQueryResponseData.getReturn_msg());
				break;
			}
			// result_code
			if (orderQueryResponseData.getResult_code() == null
					|| !orderQueryResponseData.getResult_code().equals(
							"SUCCESS")) {
				result = HttpResult.success(
						PayErrorCodeMessage.RESULT_CODE_FAIL.getIndex(),
						orderQueryResponseData.getErr_code() + "="
								+ orderQueryResponseData.getErr_code_des());
				break;
			}
			// 判断签名
			String returnedSign = orderQueryResponseData.getSign();
			if (returnedSign == null
					|| !returnedSign.equals(WxPayUtil
							.getSign(orderQueryResponseData))) {
				result = HttpResult.success(
						PayErrorCodeMessage.ERROR_SIGN.getIndex(),
						PayErrorCodeMessage.ERROR_SIGN.getName());
				break;
			}
			// 判断支付状态
			String tradeState = orderQueryResponseData.getTrade_state();
			if (tradeState != null && tradeState.equals("SUCCESS")) {
				result = HttpResult.success(200, "订单支付成功");
			} else if (tradeState == null) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_FAIL.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_FAIL.getName());
			} else if (tradeState.equals("REFUND")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_REFUND.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_REFUND.getName());
			} else if (tradeState.equals("NOTPAY")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_NOTPAY.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_NOTPAY.getName());
			} else if (tradeState.equals("CLOSED")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_CLOSED.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_CLOSED.getName());
			} else if (tradeState.equals("REVOKED")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_REVOKED.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_REVOKED.getName());
			} else if (tradeState.equals("USERPAYING")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_USERPAYING.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_USERPAYING.getName());
			} else if (tradeState.equals("PAYERROR")) {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_PAYERROR.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_PAYERROR.getName());
			} else {
				result = HttpResult.success(
						PayErrorCodeMessage.TRADE_STATE_UNKOWN.getIndex(),
						PayErrorCodeMessage.TRADE_STATE_UNKOWN.getName());
			}
			// 根据out_trade_no查询本地订单，判断本地订单支付状态
			// 如果支付成功，直接返回
			// 如果未支付成功，根据返回信息修改支付状态
		} while (false);
		return result;
	}
	
	/**
	 * 调用微信统一下单接口,返回客户端数据
	 * 
	 * @param tradeType
	 *            JSAPI支付
	 * @return UnifiedOrderResponseData
	 */
	private UnifiedOrderResponseData unifiedOrder(String userId, String proId,
			int price, String ip, String openid,int type) { // proId 问题id
		String orderId = null;
		if (type == 1) {
			IPayService payService = (IPayService) AbstractApp.getRpcService("payService");
			orderId = payService.weixinPayLog(userId, proId); // type 1
		} else if (type == 2) {
			orderId = proId;// 本地订单号
		}
		if(Strings.isNullOrEmpty(ip)){
			 InetAddress addr = null;
			try {
				addr = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	          ip = addr.getHostAddress().toString();
		}
		/*if (PayApp.theApp.isDebug()) {// 测试时候支付一分钱，买入价值6块的20分钟语音
			price = 1;//1分钱
		}*/
		// 生成请求数据对象
		UnifiedOrderRequestData data = constructData(orderId, price, ip, openid);
		// 调用微信统一下单接口
		UnifiedOrderResponseData responseData = WxPayUtil.unifiedOder(data);
		LoggerUtils.payLogger.info("UnifiedOrderResponseData => "
				+ JSONObject.toJSONString(responseData));
		return responseData;
	}
    
	/**
	 * 构建统一下单参数，发给微信服务器
	 * 
	 * @param tradeType
	 * @param body
	 * @param tradeNo
	 * @param ip
	 * @return
	 */
	private UnifiedOrderRequestData constructData(
			String tradeNo, 
			int totalFee,
			String ip, 
			String openid) {
		UnifiedOrderRequestData data = new UnifiedOrderRequestData.
				UnifiedOrderReqDataBuilder(
				WeixinConstant.FEBDA_PAY_BODY, tradeNo, totalFee, ip,
				WeixinConstant.TRADE_TYPE).setOpenid(openid).build();
		// 产生签名信息
		data.setSign(WxPayUtil.getSign(data));
		return data;
	}
	
	/**
	 * 根据统一下单接口返回的数据，生成前台JS-SDK所需的数据包
	 * @param responseData
	 * @return JSONObject
	 */
	private HttpResult<Object> getAppPackage(
			UnifiedOrderResponseData responseData) {
		if (null == responseData) {
			return null;
		}
		// return_code 为 FAIL
		if (responseData.getReturn_code() == null
				|| !WeixinConstant.SUCCESS
						.equals(responseData.getReturn_code())) {
			return HttpResult.error(
					PayErrorCodeMessage.RETURN_CODE_FAIL.getIndex(),
					responseData.getReturn_msg());
		}
		// result_code 为 FAIL
		if (responseData.getResult_code() == null
				|| !WeixinConstant.SUCCESS
						.equals(responseData.getResult_code())) {
			return HttpResult.error(
					PayErrorCodeMessage.RESULT_CODE_FAIL.getIndex(),
					responseData.getErr_code() + ":"
							+ responseData.getErr_code_des());
		}
		String responseSign = WxPayUtil.getSign(responseData);
		// 签名错误
		if (!responseSign.equals(responseData.getSign())) {
			return HttpResult.success(
					PayErrorCodeMessage.ERROR_SIGN.getIndex(),
					PayErrorCodeMessage.ERROR_SIGN.getName());
		}
		// 将数据封装成JS-SDK需要的形式返回前台
		// appId 是 String(16) wx8888888888888888 商户注册具有支付权限的公众号成功后即可获得
		String appId = WeixinPayConfig.APPID;
		// 时间戳 timeStamp 是 String(32) 1414561699
		// SimpleDateFormat simpleDateFormat = new
		// SimpleDateFormat("MMddHHmmss");
		String timeStamp = DateUtils.getTimeStamp();// simpleDateFormat.format(new Date());
		// 随机字符串 nonceStr 是 String(32) 5K8264ILTKCH16CQ2502SI8ZNMTM67VS
		// 随机字符串，不长于32位
		String nonceStr = ConceStrUtils.createConceStr();
		// 订单详情扩展字符串 package 是 String(128) prepay_id=123456789
		// 微信统一下单接口返回的prepay_id参数值
		String packageStr = "prepay_id=" + responseData.getPrepay_id();
		// 签名方式 signType 是 String(32) MD5 签名算法
		String signType = WeixinConstant.SIGN_TYPE;
		// 签名 paySign 是 String(64) C380BEC2BFD727A4B6845133519F3AD6 签名
		GetBrandWCPayRequestData getBrandWCPayRequestData = new GetBrandWCPayRequestData(
				appId, timeStamp, nonceStr, packageStr, signType);
		String paySign = getSign(appId, timeStamp, nonceStr, packageStr, signType);
//		getBrandWCPayRequestData.setPaySign(WxPayUtil
//				.getSign(getBrandWCPayRequestData));
		getBrandWCPayRequestData.setPaySign(paySign);
		return HttpResult.success(200, getBrandWCPayRequestData);
	}
	
	/**
	 * 
	 * @param appId
	 * @param timeStamp
	 * @param nonceStr
	 * @param pcakage
	 * @param signType
	 * @return
	 */
	private String getSign(String appId, String timeStamp, String nonceStr,
			String packageStr, String signType) {
		// 获取微信返回的签名
		Map<String, Object> params = ImmutableMap.<String, Object> builder()
				.put("appId", appId)
				.put("timeStamp", timeStamp) 
				.put("nonceStr", nonceStr)
				.put("package", packageStr)
				.put("signType", signType)
				.build();
		// key ASCII排序
		SortedMap<String, Object> sortMap = MapUtils.sortMap(params);
		sortMap.put("package", packageStr);
		// paySign的生成规则和Sign的生成规则同理
		/**
		 * {appId=wxe945cda35f24b4d7, nonceStr=1fWYZ1zKJeUuP33B, package=prepay_id=wx201608131234029ac45e732f0933457641, signType=MD5, timeStamp=1471062842, key=N3V6bJzsFLzQrVpfWa1u0aRdgt2Yo5Ai}
		 */
		String paySign = PayCommonUtil.createSignPublic(Charsets.UTF_8.toString(), sortMap);
		return paySign;
	}

	/**
	 * 获取用户的openid
	 * @param code
	 *        回调微信给到的code(15分钟过期)
	 * @return
	 */
	public HttpResult<String> authorize(String code) {
		try {
			if(Strings.isNullOrEmpty(code)){
				HttpResult.error(420, "important code is null or empty");
			}
			//通过code获取用户的openid
			/*String tokenParam = "grant_type=client_credential&appid="+WeixinPayConfig.APPID+"&secret="+WeixinPayConfig.APPSECRET;
			String tokenJsonStr = HttpClientUtil.SendGET("https://api.weixin.qq.com/cgi-bin/token", tokenParam);
			Map tokenMap = JSON.parseObject(tokenJsonStr);
			//获取access_token
			String access_token = (String)tokenMap.get("access_token");
			String ticketParam = "access_token="+access_token+"&type=jsapi";
			String ticketJsonStr = HttpClientUtil.SendGET("https://api.weixin.qq.com/cgi-bin/ticket/getticket", ticketParam);
			Map ticketMap = JSON.parseObject(ticketJsonStr);
			//获取jsapi_ticket
			String ticket = (String)ticketMap.get("ticket");*/
			StringBuilder openParam =new StringBuilder()
			.append("appid=")
			.append(WeixinPayConfig.APPID)
			.append("&")
			.append("secret=")
			.append("&")
			.append("code=")
			.append(code)
			.append("&")
			.append("grant_type=authorization_code");
			//获取openID
			String openJsonStr = HttpClientUtil.SendGET(WeixinPayConfig.OAUTH2_URL, openParam.toString());
			Map<String,Object> openMap = JSON.parseObject(openJsonStr);
			String openid = (String) openMap.get("openid");
			return HttpResult.success(0, openid);
		} catch (Exception e) {
			LoggerUtils.payLogger.error("用户授权失败＝{},{}"," code："+code," 异常："+e.getMessage());
			return HttpResult.error(1000, "用户授权失败");
		}
		
	}
	
	
	/**
	 * 
	 * @param outTradeNo
	 *        商户订单号
	 * @param tradeNo
	 *        微信支付订单号
	 * @param price
	 *        总金额
	 * @return
	 */
	public boolean updateDB(String outTradeNo, String transactionId, int price,int type) {
        return fendaService.updateFendaCheckoutPayed(outTradeNo);
    }
	
	
	
	
	
	public static void main(String[] args) {
		PayedMessage payokm = new PayedMessage();// 消息体
		payokm.setTradeNo("1213131232323");// 商户订单号
		payokm.setType(PayedMessaeTypeEnum.QUESTION_PAYED.getIndex());// 支付类型
		MessageSender.send(payokm, QueueEnum.FENDA_PAY.getQueueName());//放入那个队列 //注意：这里发送消息是否考虑用异步操作
	}
	
	
     
}
