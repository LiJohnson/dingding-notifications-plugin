package com.ztbsuper.wechatwork;

import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


/**
 * Created by lcs on 2019-05-28.
 */
public class WechatWorkService {
	/**
	 * 文本消息
	 */
	public static void sendMessage(String messageApiUrl,String agentid, String toUser, String message) throws UnirestException {
		JSONObject param = new JSONObject();
		JSONObject content = new JSONObject();
		content.put("content", message);
		param.put("touser", toUser);
		param.put("msgtype", "text");
		param.put("agentid", agentid);
		param.put("text", content);
		param.put("safe", "0");

		Unirest.post(messageApiUrl)
				.header("Content-Type", "application/json")
				.body(param.toJSONString())
				.asString().getBody();

	}

	/*
	 * content 内容格式
	 * {
	 * "articles" : [
	 * {
	 * "title" : "116-carnet-base-service (#5) => SUCCESS",
	 * "description" : "今年中秋节公司有豪礼相送",
	 * "url" : "http://baidu.com",
	 * "picurl" : "http://icons.iconarchive.com/icons/paomedia/small-n-flat/1024/sign-check-icon.png"
	 * }
	 * ]
	 * }
	 */
	public static void sendArticleMessage(String messageApiUrl, String agentid, String toUser, Articles... articles) throws UnirestException {
		JSONObject param = new JSONObject();
		JSONObject content = new JSONObject();
		content.put("articles", articles);
		param.put("touser", toUser);
		param.put("msgtype", "news");
		param.put("agentid", agentid);
		param.put("news", content);
		param.put("safe", "0");
		Unirest.post(messageApiUrl)
				.header("Content-Type", "application/json")
				.body(param.toJSONString())
				.asString().getBody();

	}
}
