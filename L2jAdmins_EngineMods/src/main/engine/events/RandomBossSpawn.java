/*
 * L2J_EngineMods
 * Engine developed by Fissban.
 *
 * This software is not free and you do not have permission
 * to distribute without the permission of its owner.
 *
 * This software is distributed only under the rule
 * of www.devsadmins.com.
 * 
 * Contact us with any questions by the media
 * provided by our web or email marco.faccio@gmail.com
 */
package main.engine.events;

import main.data.ConfigData;
import main.engine.AbstractMods;
import main.holders.RewardHolder;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.holder.LocationHolder;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.util.lib.Rnd;

/**
 * @author fissban
 */
public class RandomBossSpawn extends AbstractMods
{
	private static final String[] LOCATIONS =
	{
		"in the colliseum",
		"near the entrance of the Garden of Eva",
		"close to the western entrance of the Cemetary",
		"at Gludin's Harbor"
	};
	
	private static final LocationHolder[] SPAWNS =
	{
		new LocationHolder(150086, 46733, -3407),
		new LocationHolder(84805, 233832, -3669),
		new LocationHolder(161385, 21032, -3671),
		new LocationHolder(89199, 149962, -3581),
	};
	
	private static L2Npc _raid = null;
	
	/**
	 * Constructor
	 */
	public RandomBossSpawn()
	{
		registerMod(ConfigData.ENABLE_RandomBossSpawn);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				startTimer("spawnRaids", ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60, null, null, true);
				break;
			case END:
				cancelTimers("spawnRaids");
				break;
		}
	}
	
	@Override
	public void onTimer(String timerName, L2Npc npc, L2PcInstance player)
	{
		switch (timerName)
		{
			case "spawnRaids":
				int random = Rnd.get(4);
				// spawn raid
				_raid = addSpawn(ConfigData.RANDOM_BOSS_NPC_ID.get(Rnd.get(ConfigData.RANDOM_BOSS_NPC_ID.size())), SPAWNS[random], false, ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60);
				// anuncio del spawn del raid
				Broadcast.announceToOnlinePlayers("Raid " + _raid.getName() + " spawn " + LOCATIONS[random]);
				// anunciamos el tiempo que tienen para matarlo
				Broadcast.announceToOnlinePlayers("Have " + ConfigData.RANDOM_BOSS_SPWNNED_TIME + " minutes to kill");
				break;
		}
	}
	
	@Override
	public void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		if (victim == _raid)
		{
			for (RewardHolder reward : ConfigData.RANDOM_BOSS_REWARDS)
			{
				if (Rnd.get(100) <= reward.getRewardChance())
				{
					killer.sendMessage("Have won " + reward.getRewardCount() + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
					killer.getActingPlayer().getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), (L2PcInstance) killer, victim);
				}
			}
		}
	}
}
