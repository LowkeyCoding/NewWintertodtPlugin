package dev.walsted;

import dev.walsted.wintertodt.WintertodtExPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WintertodtExPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WintertodtExPlugin.class);
		RuneLite.main(args);
	}
}