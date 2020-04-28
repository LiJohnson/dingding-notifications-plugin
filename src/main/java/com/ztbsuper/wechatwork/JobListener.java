package com.ztbsuper.wechatwork;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by lcs on 2019-05-28.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {

	private final static List<String> SKIP_PROJECT_NAME = Collections.singletonList("default");

	@Override
	public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build);
		Result result = build.getResult();
		if(SKIP_PROJECT_NAME.contains(build.getProject().getDisplayName())){
			return;
		}
		if (!checkSendMessage(result, wechatWorkNotifier)) return;

		wechatWorkNotifier.sendMessage(getBuildMessage(build, result));
	}

	private static Boolean checkSendMessage(Result result, WechatWorkNotifier wechatWorkNotifier) {
		if (result == null) {
			return wechatWorkNotifier.getOnAbort();
		} else if (result.equals(Result.SUCCESS)) {
			return wechatWorkNotifier.getOnSuccess();
		} else if (result.equals(Result.FAILURE)) {
			return wechatWorkNotifier.getOnFailed();
		} else {
			return wechatWorkNotifier.getOnAbort();
		}
	}

	@Override
	public void onStarted(AbstractBuild build, TaskListener listener) {
		if (SKIP_PROJECT_NAME.contains(build.getProject().getDisplayName())) {
			return;
		}

		WechatWorkNotifier wechatWorkNotifier = getService(build);
		if (wechatWorkNotifier.getOnStart()) {
			String content = String.format("项目 [%s](%s) 开始构建", build.getProject().getDisplayName(), build.getDisplayName());
			wechatWorkNotifier.sendMessage(content);
		}
	}

	private WechatWorkNotifier getService(AbstractBuild build) {
		Optional a =build.getProject()
				.getPublishersList()
				.toMap()
				.values()
				.stream()
				.filter(publisher -> publisher instanceof WechatWorkNotifier)
				.findFirst();
		Assert.isTrue(a.isPresent(), "没有找到 WechatWorkNotifier");
		return (WechatWorkNotifier) a.get();
	}

	private static Articles getBuildMessage( AbstractBuild build, Result result) {
		Articles articles = new Articles();
		if (Result.SUCCESS.equals(result)) {
			articles.setPicurl("http://icons.iconarchive.com/icons/paomedia/small-n-flat/512/sign-check-icon.png");
		} else if (Result.FAILURE.equals(result)) {
			articles.setPicurl("http://icons.iconarchive.com/icons/paomedia/small-n-flat/512/sign-error-icon.png");
		} else {
			articles.setPicurl("http://icons.iconarchive.com/icons/paomedia/small-n-flat/512/sign-ban-icon.png");
		}
		String node = build.getBuiltOn().getNodeName();
		articles.setTitle(String.format("%s (%s) => %s", build.getProject().getDisplayName(), build.getDisplayName(), result == null ? "UNKNOWN" : result));
		articles.setDescription(String.format(
				"node:%s,\nsummary:%s,\nduration:%s", StringUtils.isBlank(node) ? node : "master",
				build.getBuildStatusSummary().message,
				build.getDurationString()
		));
		return articles;
	}

}
