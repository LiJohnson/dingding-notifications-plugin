package com.ztbsuper.wechatwork;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Created by lcs on 2019-05-28.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {
	@Override
	public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build, listener);
		Result result = build.getResult();

		if(!checkSendMessage(result, wechatWorkNotifier)) return;

		String content = String.format("【%Tc】项目 [%s](%s), 构建结果: %s", new Date(), build.getProject().getDisplayName(), build.getDisplayName(), result == null ? "null" : result);
		wechatWorkNotifier.sendMessage(content);
	}

	private static Boolean checkSendMessage(Result result, WechatWorkNotifier wechatWorkNotifier) {
		if(result == null ){
			return wechatWorkNotifier.getOnAbort();
		}else if(result.equals(Result.SUCCESS) ){
			return wechatWorkNotifier.getOnSuccess();
		}else if(result.equals(Result.FAILURE) ){
			return wechatWorkNotifier.getOnFailed();
		}else {
			return wechatWorkNotifier.getOnAbort();
		}
	}

	@Override
	public void onStarted(AbstractBuild build, TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build, listener);
		if(wechatWorkNotifier.getOnStart()){
			String content = String.format("项目 [%s](%s) 开始构建", build.getProject().getDisplayName(), build.getDisplayName());
			wechatWorkNotifier.sendMessage(content);
		}
	}

	private WechatWorkNotifier getService(AbstractBuild build, TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = null;
		wechatWorkNotifier = (WechatWorkNotifier) build.getProject()
				.getPublishersList()
				.toMap()
				.values()
				.stream()
				.filter(publisher -> publisher instanceof WechatWorkNotifier)
				.findFirst()
				.orElse(null);

		Assert.notNull(wechatWorkNotifier,"没有找到 WechatWorkNotifier");
		return wechatWorkNotifier;
	}
}
