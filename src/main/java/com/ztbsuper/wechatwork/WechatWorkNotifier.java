package com.ztbsuper.wechatwork;

import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
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
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by lcs on 2019-05-28.
 */
public class WechatWorkNotifier extends Notifier {

	private String messageApiUrl;
	private String agentid;
	private String toTag;
	private String toUser;
	private Boolean onStart;
	private Boolean onSuccess;
	private Boolean onFailed;
	private Boolean onAbort;

	@DataBoundConstructor
	public WechatWorkNotifier(String messageApiUrl, String agentid, String toTag, String toUser, Boolean onStart, Boolean onSuccess, Boolean onFailed, Boolean onAbort) {
		super();
		this.messageApiUrl = messageApiUrl;
		this.agentid = agentid;
		this.toUser = toUser;
		this.toTag = toTag;
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
		WechatWorkService.sendMessage(
				defaultVal(this.messageApiUrl, this.getDescriptor().defaultMessageApiUrl),
				defaultVal(this.agentid, this.getDescriptor().getFirstDefaultAgentid()),
				this.toTag,
				defaultVal(this.toUser, this.getDescriptor().getDefaultToUser()),
				message);
	}
	public void sendBuildLog(String fileName , InputStream  logInputStream) {
		WechatWorkService.sendLogFile(
				defaultVal(this.messageApiUrl, this.getDescriptor().defaultMessageApiUrl),
				defaultVal(this.agentid, this.getDescriptor().getFirstDefaultAgentid()),
				this.toTag,
				defaultVal(this.toUser, this.getDescriptor().getDefaultToUser()),
				fileName,logInputStream);
	}

	private static String defaultVal( String val1,String val2 ){
		return StringUtils.hasLength(val1) ? val1 : val2;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		return true;
	}

	@Override
	public WechatWorkNotifierDescriptor getDescriptor() {
		return (WechatWorkNotifierDescriptor)super.getDescriptor();
	}

	@Extension
	public static class WechatWorkNotifierDescriptor extends BuildStepDescriptor<Publisher> {
		public WechatWorkNotifierDescriptor() {
			load();
		}
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "企业微信通知器配置";
		}

		/**
		 * 默认消息api
		 */
		private String defaultMessageApiUrl;
		/**
		 * 默认发送对象
		 */
		private String defaultToUser;
		/**
		 * 默认应用
		 */
		private String defaultAgentid;

		public String getDefaultMessageApiUrl() {
			return defaultMessageApiUrl;
		}

		public void setDefaultMessageApiUrl(String defaultMessageApiUrl) {
			this.defaultMessageApiUrl = defaultMessageApiUrl;
		}

		public String getDefaultToUser() {
			return defaultToUser;
		}

		public void setDefaultToUser(String defaultToUser) {
			this.defaultToUser = defaultToUser;
		}
		public String getFirstDefaultAgentid() {
			if(defaultAgentid == null) return null;
			return defaultAgentid.split(",")[0].split(":")[0];
		}
		public String getDefaultAgentid() {
			return defaultAgentid;
		}

		public void setDefaultAgentid(String defaultAgentid) {
			this.defaultAgentid = defaultAgentid;
		}

		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			this.defaultMessageApiUrl = formData.getString("defaultMessageApiUrl");
			this.defaultAgentid = formData.getString("defaultAgentid");
			this.defaultToUser = formData.getString("defaultToUser");
			save();
			return super.configure(req, formData);
		}

		public ListBoxModel doFillAgentidItems() {
			ListBoxModel list = new ListBoxModel();

			if (!StringUtils.hasText(this.defaultAgentid)) {
				return list;
			}
			Arrays.stream(this.defaultAgentid.split(","))
					.map(item -> item.split(":"))
					.map(arr -> new ListBoxModel.Option(arr.length > 1 ? arr[1] : arr[0], arr[0]))
					.forEach(list::add);
			return list;
		}
	}

	public String getAgentid() {
		return agentid;
	}

	public String getToTag() {
		return toTag;
	}
	public String getToUser() {
		return toUser;
	}

	public Boolean getOnStart() {
		if(onStart == null) return Boolean.FALSE;
		return onStart;
	}

	public Boolean getOnSuccess() {
		if(onSuccess == null) return Boolean.FALSE;
		return onSuccess;
	}

	public Boolean getOnFailed() {
		if(onFailed == null) return Boolean.FALSE;
		return onFailed;
	}

	public Boolean getOnAbort() {
		if(onAbort == null) return Boolean.FALSE;
		return onAbort;
	}
}
