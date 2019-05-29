package com.ztbsuper.wechatwork;

import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.apache.commons.lang.StringUtils;


/**
 * Created by lcs on 2019-05-28.
 */
public class WechatWorkService extends Notifier {
	private static JSONObject ACCESS_TOKEN_INFO = new JSONObject();

	@Override

	public BuildStepMonitor getRequiredMonitorService() {

		return BuildStepMonitor.NONE;
	}


	private static String getAccessToken(String corpid, String corpsecret) throws UnirestException {
		String token = ACCESS_TOKEN_INFO.getString("access_token");
		Long expires = ACCESS_TOKEN_INFO.getLong("expire");
		if (StringUtils.isNotBlank(token) && expires != null && expires > System.currentTimeMillis()) {
			return token;
		}

		String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s", corpid, corpsecret);
		String response = Unirest.get(url).asString().getBody();
		ACCESS_TOKEN_INFO = JSONObject.parseObject(response);
		ACCESS_TOKEN_INFO.put("expire", System.currentTimeMillis() + ACCESS_TOKEN_INFO.getLong("expires_in") * 1000);
		token = ACCESS_TOKEN_INFO.getString("access_token");
		if (StringUtils.isBlank(token) ) {
			System.out.println("get access token error " + response);
			throw new RuntimeException(response);
		}
		return token;
	}

	public static void sendMessage(String corpid, String corpsecret, String agentid,String toUser , String message) throws UnirestException {
		String url = String.format("https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s", getAccessToken(corpid, corpsecret));
		JSONObject param = new JSONObject();
		JSONObject content = new JSONObject();
		content.put("content", message);
		param.put("touser", toUser);
		param.put("msgtype", "text");
		param.put("agentid", agentid);
		param.put("text", content);
		param.put("safe", "0");

		String response = Unirest.post(url)
				.header("Content-Type", "application/json")
				.body(param.toJSONString())
				.asString().getBody();

	}
}
