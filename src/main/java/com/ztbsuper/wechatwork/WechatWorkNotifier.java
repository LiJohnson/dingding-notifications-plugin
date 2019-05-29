package com.ztbsuper.wechatwork;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Created by lcs on 2019-05-28.
 */
public class WechatWorkNotifier extends Notifier {

	private String jenkinsURL;
	private String corpid;
	private String corpsecret;
	private String agentid;
	private String toUser;
	private Boolean onStart;
	private Boolean onSuccess;
	private Boolean onFailed;
	private Boolean onAbort;

	@DataBoundConstructor
	public WechatWorkNotifier(String jenkinsURL, String corpid, String corpsecret, String agentid, String toUser, Boolean onStart, Boolean onSuccess, Boolean onFailed, Boolean onAbort) {
		super();
		this.jenkinsURL = jenkinsURL;
		this.corpid = corpid;
		this.corpsecret = corpsecret;
		this.agentid = agentid;
		this.toUser = toUser;
		this.onStart = onStart;
		this.onSuccess = onSuccess;
		this.onFailed = onFailed;
		this.onAbort = onAbort;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public void sendMessage(String message) {
		try {
			WechatWorkService.sendMessage(this.corpid, this.corpsecret, this.agentid, this.toUser, message);
		} catch (Exception e) {
//			log.error("send message error", e);
		}

	}
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		return true;
	}

	@Override
	public WechatWorkNotifierDescriptor getDescriptor() {
		return (WechatWorkNotifierDescriptor) super.getDescriptor();
	}
	@Extension
	public static class WechatWorkNotifierDescriptor extends BuildStepDescriptor<Publisher> {


		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "企业微信通知器配置";
		}
	}

	public String getJenkinsURL() {
		return jenkinsURL;
	}

	public String getCorpid() {
		return corpid;
	}

	public String getCorpsecret() {
		return corpsecret;
	}

	public String getAgentid() {
		return agentid;
	}

	public String getToUser() {
		return toUser;
	}

	public Boolean getOnStart() {
		return onStart;
	}

	public Boolean getOnSuccess() {
		return onSuccess;
	}

	public Boolean getOnFailed() {
		return onFailed;
	}

	public Boolean getOnAbort() {
		return onAbort;
	}
}
