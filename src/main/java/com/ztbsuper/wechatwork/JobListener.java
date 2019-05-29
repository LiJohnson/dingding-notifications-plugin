package com.ztbsuper.wechatwork;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * Created by lcs on 2019-05-28.
 */
@Extension
public class JobListener extends RunListener<AbstractBuild> {
	@Override
	public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build, listener);
		Result result = build.getResult();
		String content = String.format("项目 %s [%s] , ", build.getProject().getDisplayName(), build.getDisplayName());
		String status  = "构建中断";
		if(result != null){
			if(wechatWorkNotifier.getOnSuccess() && Result.SUCCESS.equals(result)){
				status = "构建成功";
			}else if( wechatWorkNotifier.getOnFailed() && Result.FAILURE.equals(result) ){
				status = "构建失败";
			}
		}
		wechatWorkNotifier.sendMessage(content + status);
	}

	@Override
	public void onStarted(AbstractBuild build, TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = getService(build, listener);
		if(wechatWorkNotifier.getOnStart()){
			String content = String.format("项目[%s%s]开始构建", build.getProject().getDisplayName(), build.getDisplayName());
			wechatWorkNotifier.sendMessage(content);
		}
	}

	private WechatWorkNotifier getService(AbstractBuild build, TaskListener listener) {
		WechatWorkNotifier wechatWorkNotifier = null;
//		wechatWorkNotifier = (WechatWorkNotifier) build.getProject()
//				.getPublishersList()
//				.toMap()
//				.values()
//				.stream()
//				.filter(publisher -> publisher instanceof WechatWorkNotifier)
//				.findFirst()
//				.orElse(null);

		Assert.notNull(wechatWorkNotifier,"没有找到 WechatWorkNotifier");
		return wechatWorkNotifier;
	}
}
