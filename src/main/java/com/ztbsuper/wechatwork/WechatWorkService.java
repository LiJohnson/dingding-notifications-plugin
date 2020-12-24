package com.ztbsuper.wechatwork;

import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;


/**
 * Created by lcs on 2019-05-28.
 */

public class WechatWorkService {
	private static final Logger LOGGER = Logger.getLogger(WechatWorkService.class.getName());

	/**
	 * 文本消息
	 */
	public static void sendMessage(String messageApiUrl,String agentid, String toUser, String message) {
		JSONObject content = new JSONObject();
		content.put("content", message);
		sendMessage(messageApiUrl, agentid, toUser, "markdown",content);
	}

	/**
	 * 发送日志
	 * @param messageApiUrl
	 * @param agentid
	 * @param toUser
	 * @param fileName
	 * @throws UnirestException
	 * @throws JSONException
	 */
	public static void sendLogFile(String messageApiUrl, String agentid, String toUser, String fileName,InputStream  inputStream) {
		String media_id = null;
		try {
			media_id = Unirest.post(messageApiUrl.replace("sendMessage","uploadMedia"))
					.field("file", inputStream,fileName)
					.field("type", "file")
					.field("agentid", agentid)
					.asJson().getBody().getObject().getString("media_id");
		} catch (JSONException | UnirestException e) {
			e.printStackTrace();
			return;
		}
		JSONObject content = new JSONObject();
		content.put("media_id", media_id);
		sendMessage(messageApiUrl, agentid, toUser, "file",content);
	}

	/**
	 * 消息发送
	 * @param messageApiUrl
	 * @param agentid
	 * @param toUser
	 * @param msgType
	 * @param content
	 */
	private static void sendMessage(String messageApiUrl,String agentid, String toUser,String msgType,JSONObject content){
		JSONObject param = new JSONObject();
		param.put("touser", toUser);
		param.put("msgtype", msgType);
		param.put("agentid", agentid);
		param.put(msgType, content);
		param.put("safe", "0");

		String res = null;
		try {
			res = Unirest.post(messageApiUrl)
					.header("Content-Type", "application/json")
					.body(param.toJSONString())
					.asString().getBody();
		} catch (UnirestException e) {
			e.printStackTrace();
		} finally {
			LOGGER.finer(String.format("%s %s %s", messageApiUrl, param.toJSONString(), res));
		}
	}
}
