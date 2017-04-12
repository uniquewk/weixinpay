package com.fs.module.weixin.utils;

import java.util.Random;

/**
 * 
 * @author wangkai
 * @2016年4月6日 下午10:35:44
 * @desc:获取字母和数字的随机字符串
 */
public class ConceStrUtils {
	
	public static String createConceStr() {
		String strs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		int length = 16;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			// str +=strs.substring(0, new Random().nextInt(strs.length()));
			char achar = strs.charAt(new Random().nextInt(strs.length() - 1));
			builder.append(achar);
		}
		return builder.toString();
	}

	public static void main(String[] args) {
		String conceStr = createConceStr();
		System.out.println(conceStr);
//		String strs = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//		System.out.println(strs.length() + "==="
//				+ strs.charAt(strs.length() - 1));
	}

}
