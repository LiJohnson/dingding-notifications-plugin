package com.ztbsuper.wechatwork;

import hudson.EnvVars;
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
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lcs on 2019-05-28.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {

	/**
	 * MatrixRun的构建不进行消息发送
	 * @param build
	 * @return
	 */
	private static boolean isDefaultMatrixProjectName(AbstractBuild build){
		if ("hudson.matrix.MatrixRun".equals(build.getClass().getName()) && "default".equals(build.getProject().getName())) {
			return true;
		}
		return false;
	}

	@Override
	public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
		if (isDefaultMatrixProjectName(build)) return;
		WechatWorkNotifier wechatWorkNotifier = getService(build);
		Result result = build.getResult();
		if (!checkSendMessage(result, wechatWorkNotifier)) return;
		String status = Result.SUCCESS.equals(result) ? "☀️" : Result.FAILURE.equals(result) ? "🌧" : "❄️️";
		String node = build.getBuiltOn().getNodeName();
		String desc = String.format(
				"> **desc** `%s` `%s` `%s`",
				StringUtils.isBlank(node) ? "master" : node,
				build.getBuildStatusSummary().message,
				build.getDurationString()
		);
		if (StringUtils.isNotBlank(build.getDescription())) {
			desc = String.format("%s\n> %s", desc, build.getDescription());
		}
		String message = String.format("## %s【%s】build %s\n%s\n%s", status, build.getProject().getDisplayName(), result == null ? "UNKNOWN" : result, desc, getBuildInfo(build, listener));

		wechatWorkNotifier.sendMessage(message);
	}

	/**
	 * 判断是否发送消息
	 * @param result
	 * @param wechatWorkNotifier
	 * @return
	 */
	private static Boolean checkSendMessage(Result result, WechatWorkNotifier wechatWorkNotifier) {
		 if (Result.SUCCESS.equals(result)) {
			return wechatWorkNotifier.getOnSuccess();
		 } else if (Result.FAILURE.equals(result)) {
			 return wechatWorkNotifier.getOnFailed();
		 } else {
			 return wechatWorkNotifier.getOnAbort();
		 }
	}

	@Override
	public void onStarted(AbstractBuild build, TaskListener listener) {
		if (isDefaultMatrixProjectName(build)) return;
		WechatWorkNotifier wechatWorkNotifier = getService(build);

		if (!wechatWorkNotifier.getOnStart()) return;

		String content = String.format("## 💦‍【%s】 build started\n\n%s",
				build.getProject().getDisplayName(),
				getBuildInfo(build, listener));
		wechatWorkNotifier.sendMessage(content);

	}

	/**
	 * 通过环境变量解析参数
	 * @param env
	 * @param key
	 * @return
	 */
	private static String getEnvVal(EnvVars env, Object key) {
		if (key == null) return null;

		final String strKey = key.toString().trim();

		if (strKey.startsWith("${") && strKey.endsWith("}")) {
			return env.computeIfAbsent(strKey.substring(2, strKey.length() - 1), k -> strKey);
		}

		return strKey;
	}

	/**
	 * 环境变量
	 * @param build
	 * @param listener
	 * @return
	 */
	private static EnvVars getEnvironmentVar(AbstractBuild build, TaskListener listener) {
		try {
			return build.getEnvironment(listener);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new EnvVars();
	}

	/**
	 * 获取构建信息
	 *
	 * @param build
	 * @return
	 */
	private static String getBuildInfo(AbstractBuild build, TaskListener listener) {
		final EnvVars envVars = getEnvironmentVar(build, listener);

		String params = build.getActions(ParametersAction.class).stream()
				.map(parameterValues -> parameterValues.getAllParameters()
						.stream()
						.filter(parameterValue -> !"build_node".equals(parameterValue.getName()))
						.map(parameterValue -> String.format("> %s : `%s`", parameterValue.getName(), getEnvVal(envVars, parameterValue.getValue())))
						.collect(Collectors.joining("\n")))
				.collect(Collectors.joining("\n"));
		String url = String.format("%sjob/%s/%s/console", JenkinsLocationConfiguration.get().getUrl(), build.getProject().getDisplayName(), build.getNumber());
		if (StringUtils.isBlank(params)) {
			params = "> 🈚️";
		}
		return String.format("> **params** \n%s \n> **[build(%s)](%s)**\n>%s", params, build.getDisplayName(), url, url);
	}

	/**
	 * get WechatWorkNotifier
	 * @param build
	 * @return
	 */
	private WechatWorkNotifier getService(AbstractBuild build) {
		Optional optional = build.getProject()
				.getPublishersList()
				.toMap()
				.values()
				.stream()
				.filter(publisher -> publisher instanceof WechatWorkNotifier)
				.findFirst();
		Assert.isTrue(optional.isPresent(), "没有找到 WechatWorkNotifier");
		return (WechatWorkNotifier) optional.get();
	}
}
