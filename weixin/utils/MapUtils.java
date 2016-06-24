package com.fs.module.weixin.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
 * @author wangkai
 * @2016年6月2日 下午8:24:56
 * @desc:对map的key进行ASCII排序
 */
public class MapUtils {

	/**
	 * 对map根据key进行排序 ASCII 顺序
	 * 
	 * @param 无序的map
	 * @return
	 */
	public static SortedMap<String, Object> sortMap(Map<String, Object> map) {
        
		List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(
				map.entrySet());
		// 排序前
		/*for (int i = 0; i < infoIds.size(); i++) {
			System.out.println(infoIds.get(i).toString());
		}*/

		// 排序
		Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {
			public int compare(Map.Entry<String, Object> o1,
					Map.Entry<String, Object> o2) {
				// return (o2.getValue() - o1.getValue());//value处理
				return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		});
		// 排序后
		SortedMap<String, Object> sortmap = new TreeMap<String, Object>();
		for (int i = 0; i < infoIds.size(); i++) {
			String[] split = infoIds.get(i).toString().split("=");
			sortmap.put(split[0], split[1]);
		}
		return sortmap;
	}
	
	
	
	public void getSortMap() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("nonceStr", PayCommonUtil.CreateNoncestr());
		params.put("package", "prepay_id=" + ("prepay_id"));
		params.put("partnerId", ConfigUtil.MCH_ID);
		params.put("prepayId", ("prepay_id"));
		params.put("signType", ConfigUtil.SIGN_TYPE);
		params.put("timeStamp", Long.toString(new Date().getTime()));
		SortedMap<String, Object> sortMap = MapUtils.sortMap(params);
		System.out.println(sortMap);
	}

}
