/*
 * Copyright (c) 2018, terminatusx <jbfleischman@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2020, loldudester <HannahRyanster@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dev.walsted.wintertodt;

import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.walsted.wintertodt.config.WintertodtExNotifyDamage;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("WintertodtEx")
public interface WintertodtExConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Toggles the status overlay"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "damageNotificationColor",
		name = "Damage Notification",
		description = "Color of damage notification text in chat"
	)
	default Color damageNotificationColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		position = 2,
		keyName = "roundNotification",
		name = "Round notification",
		description = "Notifies you before the round starts (in seconds)"
	)
	@Range(
		max = 60
	)
	@Units(Units.SECONDS)
	default int roundNotification()
	{
		return 5;
	}


	@ConfigItem(
		position = 3,
		keyName = "notifyCold",
		name = "Ambient Damage Notification",
		description = "Notifies when hit by the Wintertodt's ambient cold damage"
	)
	default WintertodtExNotifyDamage notifyCold()
	{
		return WintertodtExNotifyDamage.INTERRUPT;
	}

	@ConfigItem(
		position = 4,
		keyName = "notifySnowfall",
		name = "Snowfall Damage Notification",
		description = "Notifies when hit by the Wintertodt's snowfall attack"
	)
	default WintertodtExNotifyDamage notifySnowfall()
	{
		return WintertodtExNotifyDamage.INTERRUPT;
	}

	@ConfigItem(
		position = 5,
		keyName = "notifyBrazierDamage",
		name = "Brazier Damage Notification",
		description = "Notifies when hit by the brazier breaking"
	)
	default WintertodtExNotifyDamage notifyBrazierDamage()
	{
		return WintertodtExNotifyDamage.INTERRUPT;
	}



	@ConfigItem(
		position = 6,
		keyName = "notifyFullInv",
		name = "Full Inventory Notification",
		description = "Notifies when your inventory fills up with bruma roots"
	)
	default boolean notifyFullInv()
	{
		return true;
	}

	@ConfigItem(
		position = 7,
		keyName = "notifyEmptyInv",
		name = "Empty Inventory Notification",
		description = "Notifies when you run out of bruma roots"
	)
	default boolean notifyEmptyInv()
	{
		return true;
	}

	@ConfigItem(
		position = 8,
		keyName = "notifyBrazierOut",
		name = "Brazier Extinguish Notification",
		description = "Notifies when the brazier goes out"
	)
	default boolean notifyBrazierOut()
	{
		return true;
	}

	@ConfigItem(
			position = 9,
			keyName = "highlightColor",
			name = "Highlight Color",
			description = "Color of brazier, bruma and sprout highlights"
	)
	default Color highlightColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			position = 10,
			keyName = "warningHighlightColor",
			name = "Warning Highlight Color",
			description = "Color of objects where an interaction is needed"
	)
	default Color warningHighlightColor()
	{
		return Color.ORANGE;
	}



	@ConfigItem(
			position = 11,
			keyName = "showOverlay2",
			name = "Highlights",
			description = "Toggles the brazier, bruma and sprout highlighting"
	)
	default boolean showOverlay2()
	{
		return true;
	}

	@ConfigItem(
			position = 12,
			keyName = "minHP",
			name = "Min HP",
			description = "Sets the min hp before sounding an alarm"
	)
	@Range(
			max = 99,
			min = 1
	)
	default int minHP()
	{
		return 4;
	}
}