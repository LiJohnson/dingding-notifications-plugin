package com.ztbsuper.wechatwork;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lcs on 2019-05-28.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {

	@Override
	public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build);
		Result result = build.getResult();
		if (!checkSendMessage(result, wechatWorkNotifier)) return;

		String status = Result.SUCCESS.equals(result) ? "✅" : Result.FAILURE.equals(result) ? "‼️" : "⚠️";
		String node = build.getBuiltOn().getNodeName();
		String desc = String.format(
				"### desc \n\n- node:%s\n- summary:%s\n- duration:%s", StringUtils.isBlank(node) ? "master" : node,
				build.getBuildStatusSummary().message,
				build.getDurationString()
		);
		String message =  String.format("# %s【%s】build %s\n\n%s\n\n%s", status, build.getProject().getDisplayName(), result == null ? "UNKNOWN" : result, desc, getBuildInfo(build));

		wechatWorkNotifier.sendMessage(message);
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
		WechatWorkNotifier wechatWorkNotifier = getService(build);
		if (wechatWorkNotifier.getOnStart()) {
			String content = String.format("# 🙏🏻【%s】 build started\n\n%s",
					build.getProject().getDisplayName(),
					getBuildInfo(build));
			wechatWorkNotifier.sendMessage(content);
		}
	}

	private static String getBuildInfo(AbstractBuild build){
		String params = build.getActions(ParametersAction.class).stream()
				.map(parameterValues -> parameterValues.getAllParameters().stream().map(parameterValue -> String.format("- %s : %s", parameterValue.getName(), parameterValue.getValue())).collect(Collectors.joining("\n")))
				.collect(Collectors.joining("\n"));

		String url = String.format("%sjob/%s/%s/console", JenkinsLocationConfiguration.get().getUrl(), build.getProject().getDisplayName(), build.getNumber());
		return String.format("## params \n%s \n\n### [build(%s)](%s)\n\n %s", params, build.getDisplayName(), url, url);
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
}
