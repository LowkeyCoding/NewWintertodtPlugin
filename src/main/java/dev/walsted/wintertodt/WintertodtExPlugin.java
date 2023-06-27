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

import com.google.inject.Provides;


import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import dev.walsted.wintertodt.config.WintertodtExNotifyDamage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static dev.walsted.wintertodt.config.WintertodtExNotifyDamage.ALWAYS;
import static dev.walsted.wintertodt.config.WintertodtExNotifyDamage.INTERRUPT;
import static net.runelite.api.AnimationID.CONSTRUCTION;
import static net.runelite.api.AnimationID.CONSTRUCTION_IMCANDO;
import static net.runelite.api.AnimationID.FIREMAKING;
import static net.runelite.api.AnimationID.FLETCHING_BOW_CUTTING;
import static net.runelite.api.AnimationID.IDLE;
import static net.runelite.api.AnimationID.LOOKING_INTO;
import static net.runelite.api.AnimationID.WOODCUTTING_3A_AXE;
import static net.runelite.api.AnimationID.WOODCUTTING_ADAMANT;
import static net.runelite.api.AnimationID.WOODCUTTING_BLACK;
import static net.runelite.api.AnimationID.WOODCUTTING_BRONZE;
import static net.runelite.api.AnimationID.WOODCUTTING_CRYSTAL;
import static net.runelite.api.AnimationID.WOODCUTTING_DRAGON;
import static net.runelite.api.AnimationID.WOODCUTTING_DRAGON_OR;
import static net.runelite.api.AnimationID.WOODCUTTING_GILDED;
import static net.runelite.api.AnimationID.WOODCUTTING_INFERNAL;
import static net.runelite.api.AnimationID.WOODCUTTING_IRON;
import static net.runelite.api.AnimationID.WOODCUTTING_MITHRIL;
import static net.runelite.api.AnimationID.WOODCUTTING_RUNE;
import static net.runelite.api.AnimationID.WOODCUTTING_STEEL;
import static net.runelite.api.AnimationID.WOODCUTTING_TRAILBLAZER;

import net.runelite.api.*;

import static net.runelite.api.ItemID.BRUMA_KINDLING;
import static net.runelite.api.ItemID.BRUMA_ROOT;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(
	name = "WintertodtEx",
	description = "Show helpful information for the Wintertodt boss",
	tags = {"minigame", "firemaking", "boss"}
)
@Slf4j
public class WintertodtExPlugin extends Plugin
{
	public static final int WINTERTODT_REGION = 6462;
	public static final int BROKEN_BRAZIER = 32516;
	public static final int UNLIT_BRAZIER = 29312;
	public static final int BRAZIER = 29314;
	public static final int SNOWFALL = 502;
	public static final int METEOR = 26690;
	public static final int BRUMA_ROOTS = 29311;
	public static final int SPROUTING_ROOTS = 29315;
	@Inject
	private Notifier notifier;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WintertodtExOverlay overlay;

	@Inject
	private GameObjectOverlay overlay2;

	@Inject
	private WintertodtExConfig config;

	@Getter
	private final List<GameObject> objects = new ArrayList<>();

	@Getter
	private final List<NPC> npcs = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private WintertodtExActivity currentActivity = WintertodtExActivity.IDLE;

	@Getter(AccessLevel.PACKAGE)
	private int inventoryScore;

	@Getter(AccessLevel.PACKAGE)
	private int totalPotentialinventoryScore;

	@Getter(AccessLevel.PACKAGE)
	private int numLogs;

	@Getter(AccessLevel.PACKAGE)
	private int numKindling;

	@Getter(AccessLevel.PACKAGE)
	private boolean isInWintertodt;

	@Getter(AccessLevel.PACKAGE)
	private boolean snowfallBrazierActive;
	@Getter(AccessLevel.PACKAGE)
	private boolean meteorBrazierActive;

	@Getter(AccessLevel.PACKAGE)
	private GameObject closets_brazier;

	@Getter(AccessLevel.PACKAGE)
	private GameObject closets_root;

	@Getter(AccessLevel.PACKAGE)
	private GameObject closets_sprout;
	private boolean needRoundNotif;

	private Instant lastActionTime;

	private Instant lastTickTime;

	private int previousTimerValue;

	@Provides
	WintertodtExConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WintertodtExConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		reset();
		overlayManager.add(overlay);
		overlayManager.add(overlay2);
	}

	public void playSound(String file){
		File lol = new File(System.getProperty("user.dir")+"./sounds/" + file + ".wav");

		try{
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(lol));
			clip.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(overlay2);
		reset();
	}

	private void reset()
	{
		inventoryScore = 0;
		totalPotentialinventoryScore = 0;
		numLogs = 0;
		numKindling = 0;
		currentActivity = WintertodtExActivity.IDLE;
		closets_brazier = null;
		closets_root = null;
		lastActionTime = null;
		lastTickTime = null;
	}

	private boolean isInWintertodtRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			return client.getLocalPlayer().getWorldLocation().getRegionID() == WINTERTODT_REGION;
		}

		return false;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!isInWintertodtRegion())
		{
			if (isInWintertodt)
			{
				log.debug("Left Wintertodt!");
				reset();
				isInWintertodt = false;
				needRoundNotif = true;
			}
			return;
		}

		if (!isInWintertodt)
		{
			reset();
			log.debug("Entered Wintertodt!");
			isInWintertodt = true;
		}
		if(lastTickTime == null) {
			lastTickTime = Instant.now();
		}
		Duration actionTimeout = Duration.ofMillis(100);
		var since = Duration.between(lastTickTime, Instant.now());
		if (since.compareTo(actionTimeout) >= 0){
			if(snowfallBrazierActive && meteorBrazierActive) {
				playSound("break");
				snowfallBrazierActive = false;
				meteorBrazierActive = false;
				//log.info("broken");
				lastTickTime = Instant.now();
			}

			if(snowfallBrazierActive) {
				playSound("unlit");
				snowfallBrazierActive = false;
				//log.info("unlit");
				lastTickTime = Instant.now();
			}
			updateClosestObjects();
			var hp = client.getBoostedSkillLevel(Skill.HITPOINTS) + client.getVarbitValue(Varbits.NMZ_ABSORPTION);
			if (hp <= config.minHP()) {
				playSound("hp");
				lastTickTime = Instant.now();
			}
		}

		checkActionTimeout();
	}

	public void debugMessage(String message) {
		client.addChatMessage(ChatMessageType.PLAYERRELATED, "", "[DEBUG] " + message, "");
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated object) {
		var obj = object.getGraphicsObject();
		var loc = WorldPoint.fromLocal(client, obj.getLocation());
		if (closets_brazier != null) {
			var brazier = closets_brazier.getWorldLocation();
			brazier = brazier.dx(-1);
			brazier = brazier.dy(-1);
			if (obj.getId() == SNOWFALL) {
				//log.info("Graphic LOC: " + loc + " BA: " + brazier + " DIST: " + loc.distanceTo(brazier));
				if (loc.distanceTo(brazier) == 0) {
					//log.info("Snow fall on corner");
					snowfallBrazierActive = true;
				}
			}
		}
	}
	// x=1638, y=3997 without

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned object) {
		objects.remove(object.getGameObject());
	}
	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned object) {
		objects.add(object.getGameObject());
		var obj = object.getGameObject();
		if (closets_brazier != null) {
			var brazier = closets_brazier.getWorldLocation();
			brazier = brazier.dx(1);
			var loc = obj.getWorldLocation();
			if (obj.getId() == METEOR) {
				//log.info("OBJ LOC: " + loc + " BA: " + brazier + " DIST: " + loc.distanceTo(brazier));
				if (loc.distanceTo(brazier) == 0) {
					//log.info("Snow meteor");
					meteorBrazierActive = true;
				}
			}
		}
	}

	public void updateClosestObjects() {
		var player_loc = client.getLocalPlayer().getWorldLocation();
		var brazier = closets_brazier;
		var root = closets_root;
		var sprout = closets_sprout;
		for (GameObject obj : objects) {
			var id = obj.getId();
			if (brazier == null || !objects.contains(brazier) || player_loc.distanceTo(brazier.getWorldLocation()) > 10) {
				if (id == BRAZIER || id == UNLIT_BRAZIER || id == BROKEN_BRAZIER) {
					var loc = obj.getWorldLocation();
					if (player_loc.distanceTo(loc) < 10) {
						closets_brazier = obj;
					}
				}
			}
			if (root == null || !objects.contains(root) || player_loc.distanceTo(root.getWorldLocation()) > 10) {
				if (id == BRUMA_ROOTS) {
					var loc = obj.getWorldLocation();
					if (player_loc.distanceTo(loc) < 10) {
						closets_root = obj;
					}
				}
			}

			if (sprout == null ||  !objects.contains(sprout) || player_loc.distanceTo(sprout.getWorldLocation()) > 20) {
				if (id == SPROUTING_ROOTS) {
					var loc = obj.getWorldLocation();
					if (player_loc.distanceTo(loc) < 20) {
						closets_sprout = obj;
					}
				}
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		npcs.add(npcSpawned.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		npcs.remove(npcDespawned.getNpc());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getVarbitId() == Varbits.WINTERTODT_TIMER)
		{
			int timeToNotify = config.roundNotification();
			// Sometimes wt var updates are sent to players even after leaving wt.
			// So only notify if in wt or after just having left.
			if (timeToNotify > 0 && (isInWintertodt || needRoundNotif))
			{
				int timeInSeconds = varbitChanged.getValue() * 30 / 50;
				int prevTimeInSeconds = previousTimerValue * 30 / 50;

				log.debug("Seconds left until round start: {}", timeInSeconds);

				if (prevTimeInSeconds > timeToNotify && timeInSeconds <= timeToNotify)
				{
					notifier.notify("Wintertodt round is about to start");
					needRoundNotif = false;
				}
			}

			previousTimerValue = varbitChanged.getValue();
		}
	}

	private void checkActionTimeout()
	{
		if (currentActivity == WintertodtExActivity.IDLE)
		{
			return;
		}

		int currentAnimation = client.getLocalPlayer() != null ? client.getLocalPlayer().getAnimation() : -1;
		if (currentAnimation != IDLE || lastActionTime == null)
		{
			return;
		}

		Duration actionTimeout = Duration.ofSeconds(3);
		Duration sinceAction = Duration.between(lastActionTime, Instant.now());

		if (sinceAction.compareTo(actionTimeout) >= 0)
		{
			log.debug("Activity timeout!");
			currentActivity =WintertodtExActivity.IDLE;
		}


	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (!isInWintertodt)
		{
			return;
		}

		ChatMessageType chatMessageType = chatMessage.getType();

		if (chatMessageType != ChatMessageType.GAMEMESSAGE && chatMessageType != ChatMessageType.SPAM)
		{
			return;
		}

		MessageNode messageNode = chatMessage.getMessageNode();
		final WintertodtExInterruptType interruptType;

		if (messageNode.getValue().startsWith("You carefully fletch the root"))
		{
			setActivity(WintertodtExActivity.FLETCHING);
			return;
		}

		if (messageNode.getValue().startsWith("The cold of"))
		{
			interruptType = WintertodtExInterruptType.COLD;
		}
		else if (messageNode.getValue().startsWith("The freezing cold attack"))
		{
			interruptType = WintertodtExInterruptType.SNOWFALL;
		}

		else if (messageNode.getValue().startsWith("You have run out of bruma roots"))
		{
			interruptType = WintertodtExInterruptType.OUT_OF_ROOTS;
		}
		else if (messageNode.getValue().startsWith("Your inventory is too full"))
		{
			interruptType = WintertodtExInterruptType.INVENTORY_FULL;
		}
		else if (messageNode.getValue().startsWith("You fix the brazier"))
		{
			interruptType = WintertodtExInterruptType.FIXED_BRAZIER;
		}
		else if (messageNode.getValue().startsWith("You light the brazier"))
		{
			interruptType = WintertodtExInterruptType.LIT_BRAZIER;
		}
		else if (messageNode.getValue().startsWith("The brazier has gone out."))
		{
			interruptType = WintertodtExInterruptType.BRAZIER_WENT_OUT;
		}
		else
		{
			return;
		}

		boolean wasInterrupted = false;
		boolean neverNotify = false;

		switch (interruptType)
		{
			case COLD:
			case SNOWFALL:

				// Recolor message for damage notification
				messageNode.setRuneLiteFormatMessage(ColorUtil.wrapWithColorTag(messageNode.getValue(), config.damageNotificationColor()));
				client.refreshChat();

				// all actions except woodcutting and idle are interrupted from damage
				if (currentActivity != WintertodtExActivity.WOODCUTTING && currentActivity != WintertodtExActivity.IDLE)
				{
					wasInterrupted = true;
					playSound("idle");
				}

				break;
			case INVENTORY_FULL:
			case OUT_OF_ROOTS:
			case BRAZIER_WENT_OUT:
				wasInterrupted = true;
				break;
			case LIT_BRAZIER:
			case FIXED_BRAZIER:
				wasInterrupted = true;
				neverNotify = true;
				break;
		}

		if (!neverNotify)
		{
			boolean shouldNotify = false;
			switch (interruptType)
			{
				case COLD:
					WintertodtExNotifyDamage notify = config.notifyCold();
					shouldNotify = notify == ALWAYS || (notify == INTERRUPT && wasInterrupted);
					break;
				case SNOWFALL:
					notify = config.notifySnowfall();
					shouldNotify = notify == ALWAYS || (notify == INTERRUPT && wasInterrupted);
					break;
				case INVENTORY_FULL:
					shouldNotify = config.notifyFullInv();
					playSound("full");
					break;
				case OUT_OF_ROOTS:
					shouldNotify = config.notifyEmptyInv();
					playSound("empty");
					break;
			}

			if (shouldNotify)
			{
				notifyInterrupted(interruptType, wasInterrupted);
			}
		}

		if (wasInterrupted)
		{
			currentActivity = WintertodtExActivity.IDLE;
		}
	}

	private void notifyInterrupted(WintertodtExInterruptType interruptType, boolean wasActivityInterrupted)
	{
		final StringBuilder str = new StringBuilder();

		str.append("Wintertodt: ");

		if (wasActivityInterrupted)
		{
			str.append(currentActivity.getActionString());
			str.append(" interrupted! ");
		}

		str.append(interruptType.getInterruptSourceString());

		String notification = str.toString();
		log.debug("Sending notification: {}", notification);
		notifier.notify(notification);
	}

	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
		if (!isInWintertodt)
		{
			return;
		}

		final Player local = client.getLocalPlayer();

		if (event.getActor() != local)
		{
			return;
		}

		final int animId = local.getAnimation();
		switch (animId)
		{
			case WOODCUTTING_BRONZE:
			case WOODCUTTING_IRON:
			case WOODCUTTING_STEEL:
			case WOODCUTTING_BLACK:
			case WOODCUTTING_MITHRIL:
			case WOODCUTTING_ADAMANT:
			case WOODCUTTING_RUNE:
			case WOODCUTTING_GILDED:
			case WOODCUTTING_DRAGON:
			case WOODCUTTING_DRAGON_OR:
			case WOODCUTTING_INFERNAL:
			case WOODCUTTING_3A_AXE:
			case WOODCUTTING_CRYSTAL:
			case WOODCUTTING_TRAILBLAZER:
				setActivity(WintertodtExActivity.WOODCUTTING);
				break;

			case FLETCHING_BOW_CUTTING:
				setActivity(WintertodtExActivity.FLETCHING);
				break;

			case LOOKING_INTO:
				setActivity(WintertodtExActivity.FEEDING_BRAZIER);
				break;

			case FIREMAKING:
				setActivity(WintertodtExActivity.LIGHTING_BRAZIER);
				break;

			case CONSTRUCTION:
			case CONSTRUCTION_IMCANDO:
				setActivity(WintertodtExActivity.FIXING_BRAZIER);
				break;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer container = event.getItemContainer();

		if (!isInWintertodt || container != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}

		final Item[] inv = container.getItems();

		inventoryScore = 0;
		totalPotentialinventoryScore = 0;
		numLogs = 0;
		numKindling = 0;

		for (Item item : inv)
		{
			inventoryScore += getPoints(item.getId());
			totalPotentialinventoryScore += getPotentialPoints(item.getId());

			switch (item.getId())
			{
				case BRUMA_ROOT:
					++numLogs;
					break;
				case BRUMA_KINDLING:
					++numKindling;
					break;
			}
		}

		//If we're currently fletching but there are no more logs, go ahead and abort fletching immediately
		if (numLogs == 0 && currentActivity == WintertodtExActivity.FLETCHING)
		{
			currentActivity = WintertodtExActivity.IDLE;
		}
		//Otherwise, if we're currently feeding the brazier but we've run out of both logs and kindling, abort the feeding activity
		else if (numLogs == 0 && numKindling == 0 && currentActivity == WintertodtExActivity.FEEDING_BRAZIER)
		{
			currentActivity = WintertodtExActivity.IDLE;
		}
	}

	private void setActivity(WintertodtExActivity action)
	{
		currentActivity = action;
		lastActionTime = Instant.now();
	}

	private static int getPoints(int id)
	{
		switch (id)
		{
			case BRUMA_ROOT:
				return 10;
			case BRUMA_KINDLING:
				return 25;
			default:
				return 0;
		}
	}

	private static int getPotentialPoints(int id)
	{
		switch (id)
		{
			case BRUMA_ROOT:
			case BRUMA_KINDLING:
				return 25;
			default:
				return 0;
		}
	}
}
