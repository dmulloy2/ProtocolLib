package com.comphenix.protocol.injector;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import com.comphenix.protocol.injector.PluginVerifier.VerificationResult;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

// Damn final classes
@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest(PluginDescriptionFile.class)
public class PluginVerifierTest {
	@Test
	public void testDependecies() {
		List<Plugin> plugins = Lists.newArrayList();
		Server server = mockServer(plugins);
		
		Plugin library = mockPlugin(server, "ProtocolLib", PluginLoadOrder.POSTWORLD);
		Plugin skillPlugin = mockPlugin(server, "SkillPlugin", "RaidCraft-API", "RCPermissions", "RCConversations");
		Plugin raidCraftAPI = mockPlugin(server, "RaidCraft-API", "WorldGuard", "WorldEdit");
		Plugin conversations = mockPlugin(server, "RCConversations", "RaidCraft-API");
		Plugin permissions = mockPlugin(server, "RCPermissions", "RaidCraft-API");
		
		// Add the plugins
		plugins.addAll(Arrays.asList(library, skillPlugin, raidCraftAPI, conversations, permissions));
		PluginVerifier verifier = new PluginVerifier(library);
		
		// Verify the root - it should have no dependencies on ProtocolLib
		assertEquals(VerificationResult.NO_DEPEND, verifier.verify(skillPlugin));
	}
	
	private Server mockServer(final List<Plugin> plugins) {
		Server mockServer = mock(Server.class);
		PluginManager manager = mock(PluginManager.class);

		when(mockServer.getPluginManager()).thenReturn(manager);
		when(manager.getPlugin(anyString())).thenAnswer(new Answer<Plugin>() {
			@Override
			public Plugin answer(InvocationOnMock invocation) throws Throwable {
				String name = (String) invocation.getArguments()[0];
				
				for (Plugin plugin : plugins) {
					if (Objects.equal(name, plugin.getName())) {
						return plugin;
					}
				}
				return null;
			}
		});
		return mockServer;
	}
	
	private Plugin mockPlugin(Server server, String name,String... depend) {
		return mockPlugin(server, name, PluginLoadOrder.POSTWORLD, depend);
	}
	
	private Plugin mockPlugin(Server server, String name, PluginLoadOrder order, String... depend) {
		Plugin plugin = mock(Plugin.class);
		PluginDescriptionFile file = mock(PluginDescriptionFile.class);
		
		when(plugin.getServer()).thenReturn(server);
		when(plugin.getName()).thenReturn(name);
		when(plugin.toString()).thenReturn(name);
		when(plugin.getDescription()).thenReturn(file);
	
		// This is the difficult part
		when(file.getLoad()).thenReturn(order);
		when(file.getDepend()).thenReturn(Arrays.asList(depend));
		when(file.getSoftDepend()).thenReturn(null);
		return plugin;
	}
}
