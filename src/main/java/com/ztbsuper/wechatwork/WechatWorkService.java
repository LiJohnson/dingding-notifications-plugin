package com.ztbsuper.wechatwork;

import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.logging.Logger;


/**
 * Created by lcs on 2019-05-28.
 */

public class WechatWorkService {
	private static final Logger LOGGER = Logger.getLogger(WechatWorkService.class.getName());
	/**
	 * 文本消息
	 */
	public static void sendMessage(String messageApiUrl,String agentid, String toUser, String message) throws UnirestException {
		JSONObject param = new JSONObject();
		JSONObject content = new JSONObject();
		content.put("content", message);
		param.put("touser", toUser);
		param.put("msgtype", "markdown");
		param.put("agentid", agentid);
		param.put("markdown", content);
		param.put("safe", "0");

		String res = Unirest.post(messageApiUrl)
				.header("Content-Type", "application/json")
				.body(param.toJSONString())
				.asString().getBody();
		LOGGER.finer(String.format("%s %s %s", messageApiUrl, param.toJSONString(), res));
	}
}
