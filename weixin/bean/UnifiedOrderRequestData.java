package com.fs.module.weixin.bean;

import java.lang.reflect.Field;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fs.module.weixin.bean.config.WeixinPayConfig;
import com.fs.module.weixin.utils.ConceStrUtils;
import com.fs.module.weixin.utils.PayCommonUtil;

/**
 * 除被扫支付场景以外，商户系统先调用该接口在微信支付服务后台生成预支付交易单， 返回正确的预支付交易回话标识后再按扫码、JSAPI、
 * APP等不同场景生成交易串调起支付。
 * 
 * @type_name:UnifiedOrderRequestData
 * @time:下午2:38:55
 * @author:kevin 用构建模式改造
 */
public class UnifiedOrderRequestData {

	// 公众账号ID appid 是 String(32) wx8888888888888888
	// 微信分配的公众账号ID（企业号corpid即为此appId）
	private String appid;
	// 商户号 mch_id 是 String(32) 1900000109 微信支付分配的商户号
	private String mch_id;
	// 设备号 device_info 否 String(32) 013467007045764
	// 终端设备号(门店号或收银设备ID)，注意：PC网页或公众号内支付请传"WEB"
	private String device_info;
	// 随机字符串 nonce_str 是 String(32) 5K8264ILTKCH16CQ2502SI8ZNMTM67VS
	// 随机字符串，不长于32位。推荐随机数生成算法
	private String nonce_str;
	// 签名 sign 是 String(32) C380BEC2BFD727A4B6845133519F3AD6 签名，详见签名生成算法
	private String sign;
	// 商品描述 body 是 String(32) Ipad mini 16G 白色 商品或支付单简要描述
	private String body;
	// 商品详情 detail 否 String(8192) Ipad mini 16G 白色 商品名称明细列表
	private String detail;
	// 附加数据 attach 否 String(127) 说明 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
	private String attach;
	// 商户订单号 out_trade_no 是 String(32) 1217752501201407033233368018
	// 商户系统内部的订单号,32个字符内、可包含字母, 其他说明见商户订单号
	private String out_trade_no;
	// 货币类型 fee_type 否 String(16) CNY 符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
	private String fee_type;
	// 总金额 total_fee 是 Int 888 订单总金额，只能为整数，详见支付金额
	private int total_fee;
	// 终端IP spbill_create_ip 是 String(16) 8.8.8.8
	// APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
	private String spbill_create_ip;
	// 交易起始时间 time_start 否 String(14) 20091225091010
	// 订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
	private String time_start;
	// 交易结束时间 time_expire 否 String(14) 20091227091010
	// 订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。其他详见时间规则
	// 注意：最短失效时间间隔必须大于5分钟
	private String time_expire;
	// 商品标记 goods_tag 否 String(32) WXG 商品标记，代金券或立减优惠功能的参数，说明详见代金券或立减优惠
	private String goods_tag;
	// 通知地址 notify_url 是 String(256) http://www.baidu.com 接收微信支付异步通知回调地址
	private String notify_url;
	// 交易类型 trade_type 是 String(16) JSAPI 取值如下：JSAPI，NATIVE，APP，WAP,详细说明见参数规定
	private String trade_type;
	// /商品ID product_id 否 String(32) 12235413214070356458058
	// trade_type=NATIVE，此参数必传。此id为二维码中包含的商品ID，商户自行定义。
	private String product_id;
	// 指定支付方式 limit_pay 否 String(32) no_credit no_credit--指定不能使用信用卡支付
	private String limit_pay;
	// 用户标识 openid 否 String(128) oUpF8uMuAJO_M2pxb1Q9zNjWeS6o
	// trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。
	// 下单前需要调用【网页授权获取用户信息】接口获取到用户的Openid。
	// 企业号请使用【企业号OAuth2.0接口】获取企业号内成员userid，再调用【企业号userid转openid接口】进行转换
	private String openid;

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

	public String getDevice_info() {
		return device_info;
	}

	public void setDevice_info(String device_info) {
		this.device_info = device_info;
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getFee_type() {
		return fee_type;
	}

	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}

	public int getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(int total_fee) {
		this.total_fee = total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}

	public String getTime_start() {
		return time_start;
	}

	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}

	public String getGoods_tag() {
		return goods_tag;
	}

	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getTrade_type() {
		return trade_type;
	}

	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getLimit_pay() {
		return limit_pay;
	}

	public void setLimit_pay(String limit_pay) {
		this.limit_pay = limit_pay;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
	public SortedMap<String, Object> toMap() {
        SortedMap<String, Object> map = new TreeMap<String,Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if (obj != null) {
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

	private UnifiedOrderRequestData(UnifiedOrderReqDataBuilder builder) {
		this.appid = builder.appid;
		this.mch_id = builder.mch_id;
		this.device_info = builder.device_info;
		this.nonce_str = PayCommonUtil.CreateNoncestr();
		this.body = builder.body;
		this.detail = builder.detail;
		this.attach = builder.attach;
		this.out_trade_no = builder.out_trade_no;
		this.fee_type = builder.fee_type;
		this.total_fee = builder.total_fee;
		this.spbill_create_ip = builder.spbill_create_ip;
		this.time_start = builder.time_start;
		this.time_expire = builder.time_expire;
		this.goods_tag = builder.goods_tag;
		this.notify_url = builder.notify_url;
		this.trade_type = builder.trade_type;
		this.product_id = builder.product_id;
		this.limit_pay = builder.limit_pay;
		this.openid = builder.openid;
		this.nonce_str = builder.nonce_str;
		this.sign = PayCommonUtil.createSign("UTF-8", toMap());
	}

	public static class UnifiedOrderReqDataBuilder {
		private String appid;
		private String mch_id;
		private String device_info;
		private String body;
		private String detail;
		private String attach;
		private String out_trade_no;
		private String fee_type;
		private int total_fee;
		private String spbill_create_ip;
		private String time_start;
		private String time_expire;
		private String goods_tag;
		private String notify_url;
		private String trade_type;
		private String product_id;
		private String limit_pay;
		private String openid;
		private String nonce_str;

		/**
		 * 分享一下这样设计的好处:不暴露机密信息 ，同时区分出那些是外部传入信息和本地服务器信息
		 * 
		 * @param body
		 *            //商品描述
		 * @param out_trade_no
		 *            //订单号
		 * @param total_fee
		 *            //总的金额
		 * @param spbill_create_ip
		 *            //ip地址
		 * @param trade_type
		 *            //支付类型
		 */
		public UnifiedOrderReqDataBuilder(String body, String out_trade_no,
				Integer total_fee, String spbill_create_ip, String trade_type) {

			this(WeixinPayConfig.APPID, WeixinPayConfig.MCHID, WeixinPayConfig.NOTIFY_URL,
					body, out_trade_no, total_fee, spbill_create_ip, trade_type);
		}

		public UnifiedOrderReqDataBuilder(String appid, String mch_id,
				String notify_url, String body, String out_trade_no,
				Integer total_fee, String spbill_create_ip, String trade_type) {
			// 校验外部传入数据
			if (appid == null) {
				throw new IllegalArgumentException("传入参数appid不能为null");
			}
			if (mch_id == null) {
				throw new IllegalArgumentException("传入参数mch_id不能为null");
			}
			if (body == null) {
				throw new IllegalArgumentException("传入参数body不能为null");
			}
			if (out_trade_no == null) {
				throw new IllegalArgumentException("传入参数out_trade_no不能为null");
			}
			if (total_fee == null) {
				throw new IllegalArgumentException("传入参数total_fee不能为null");
			}
			if (spbill_create_ip == null) {
				throw new IllegalArgumentException(
						"传入参数spbill_create_ip不能为null");
			}
			if (trade_type == null) {
				throw new IllegalArgumentException("传入参数trade_type不能为null");
			}
			this.appid = appid;
			this.mch_id = mch_id;
			this.body = body;
			this.out_trade_no = out_trade_no;
			this.total_fee = total_fee;
			this.spbill_create_ip = spbill_create_ip;
			this.notify_url = notify_url;
			this.trade_type = trade_type;
			this.nonce_str = ConceStrUtils.createConceStr();
		}

		// 添加额外信息
		public UnifiedOrderReqDataBuilder setDevice_info(String device_info) {
			this.device_info = device_info;
			return this;
		}

		public UnifiedOrderReqDataBuilder setDetail(String detail) {
			this.detail = detail;
			return this;
		}

		public UnifiedOrderReqDataBuilder setAttach(String attach) {
			this.attach = attach;
			return this;
		}

		public UnifiedOrderReqDataBuilder setFee_type(String fee_type) {
			this.fee_type = fee_type;
			return this;
		}

		public UnifiedOrderReqDataBuilder setTime_start(String time_start) {
			this.time_start = time_start;
			return this;
		}

		public UnifiedOrderReqDataBuilder setTime_expire(String time_expire) {
			this.time_expire = time_expire;
			return this;
		}

		public UnifiedOrderReqDataBuilder setGoods_tag(String goods_tag) {
			this.goods_tag = goods_tag;
			return this;
		}

		public UnifiedOrderReqDataBuilder setProduct_id(String product_id) {
			this.product_id = product_id;
			return this;
		}

		public UnifiedOrderReqDataBuilder setLimit_pay(String limit_pay) {
			this.limit_pay = limit_pay;
			return this;
		}

		public UnifiedOrderReqDataBuilder setOpenid(String openid) {
			this.openid = openid;
			return this; 
		}

		public UnifiedOrderRequestData build() {

			if ("JSAPI".equals(this.trade_type) && this.openid == null) {
				throw new IllegalArgumentException(
						"当传入trade_type为JSAPI时，openid为必填参数");
			}
			if ("NATIVE".equals(this.trade_type) && this.product_id == null) {
				throw new IllegalArgumentException(
						"当传入trade_type为NATIVE时，product_id为必填参数");
			}
			// app 支付不需要以上参数 oh i got it 每个平台相应支付所需请求参数是不一样哒 请留意官方api
			return new UnifiedOrderRequestData(this);
		}

	}

	@Override
	public String toString() {
		return "UnifiedOrderRequestData [appid=" + appid + ", mch_id=" + mch_id + ", device_info=" + device_info
				+ ", nonce_str=" + nonce_str + ", sign=" + sign + ", body=" + body + ", detail=" + detail + ", attach="
				+ attach + ", out_trade_no=" + out_trade_no + ", fee_type=" + fee_type + ", total_fee=" + total_fee
				+ ", spbill_create_ip=" + spbill_create_ip + ", time_start=" + time_start + ", time_expire="
				+ time_expire + ", goods_tag=" + goods_tag + ", notify_url=" + notify_url + ", trade_type=" + trade_type
				+ ", product_id=" + product_id + ", limit_pay=" + limit_pay + ", openid=" + openid + "]";
	}

}