package com.github.j5ik2o;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

@Path("/globalConfig")
public class IrcBotGlobalConfigResource {
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public IrcBotGlobalConfigResource(UserManager userManager,
			PluginSettingsFactory pluginSettingsFactory,
			TransactionTemplate transactionTemplate) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username != null
				&& !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(
				transactionTemplate
						.execute(new TransactionCallback() {
							public Object doInTransaction() {
								PluginSettings settings = pluginSettingsFactory
										.createGlobalSettings();
								IrcBotGlobalConfig config = new IrcBotGlobalConfig();
								boolean enable = Boolean
										.parseBoolean((String) settings
												.get(IrcBotGlobalConfig.class
														.getName()
														+ ".enable"));
								config.setEnable(enable);
								
								config.setIrcServerName((String) settings
										.get(IrcBotGlobalConfig.class.getName()
												+ ".ircServerName"));

								String ircServerPort = (String) settings
										.get(IrcBotGlobalConfig.class.getName()
												+ ".ircServerPort");
								if (ircServerPort != null) {
									config.setIrcServerPort(Integer
											.parseInt(ircServerPort));
								}
								return config;
							}
						})).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final IrcBotGlobalConfig config,
			@Context HttpServletRequest request) {
		String username = userManager.getRemoteUsername(request);
		if (username != null
				&& !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback() {
			public Object doInTransaction() {
				PluginSettings pluginSettings = pluginSettingsFactory
						.createGlobalSettings();
				pluginSettings.put(
						IrcBotGlobalConfig.class.getName() 
								 + ".enable", config.getEnable().toString());
				pluginSettings.put(IrcBotGlobalConfig.class.getName()
						+ ".ircServerName", config.getIrcServerName());
				pluginSettings.put(IrcBotGlobalConfig.class.getName()
						+ ".ircServerPort",
						Integer.toString(config.getIrcServerPort()));
				return null;
			}
		});

		return Response.noContent().build();
	}
}