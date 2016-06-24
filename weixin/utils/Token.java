package com.fs.module.weixin.utils;

/**
 * 
 * @author wangkai
 * @2016年5月31日 下午8:52:23
 * @desc:TOKEN
 */
public class Token {

	private String accessToken;
	private Integer expiresIn;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Integer getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}

}
