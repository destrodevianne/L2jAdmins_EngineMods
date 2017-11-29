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
package main.engine.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.data.ConfigData;
import main.engine.AbstractMods;
import main.holders.RewardHolder;
import main.util.Util;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2.SayType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.util.lib.Rnd;

/**
 * @author fissban
 */
public class PvpReward extends AbstractMods
{
	public class PvPHolder
	{
		public int _victim;
		public long _time;
		
		public PvPHolder(int victim, long time)
		{
			_victim = victim;
			_time = time;
		}
	}
	
	// variable encargada de llevar las victimas y el tiempo en q murieron.
	private static Map<Integer, List<PvPHolder>> _pvp = new HashMap<>();
	
	/**
	 * Constructor
	 */
	public PvpReward()
	{
		registerMod(ConfigData.ENABLE_PvpReward);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		if (!Util.areObjectType(L2PcInstance.class, victim) || killer.getActingPlayer() == null)
		{
			return;
		}
		
		L2PcInstance killerPc = killer.getActingPlayer();
		
		// chequeamos si este personaje gano algun pvp
		if (!_pvp.containsKey(killerPc.getObjectId()))
		{
			// inicializamos la lista de victimas
			_pvp.put(killerPc.getObjectId(), new ArrayList<>());
		}
		
		// chequeamos el listado de victimas de killer y el tiempo transcurrido.
		for (PvPHolder pvp : _pvp.get(killer.getObjectId()))
		{
			// si encontramos que mato alguna ves a este player, chequeamos hace cuanto fue.
			if (pvp._victim == victim.getObjectId())
			{
				if (pvp._time + ConfigData.PVP_TIME < System.currentTimeMillis())
				{
					// entregamos el premio
					giveRewards(killerPc, (L2PcInstance) victim);
					// volvemos a definir el tiempo
					pvp._time = System.currentTimeMillis();
				}
				return;
			}
		}
		
		// si llegamos aqui es porque es el primer kill a este player.
		_pvp.get(killerPc.getObjectId()).add(new PvPHolder(victim.getObjectId(), System.currentTimeMillis()));
	}
	
	/**
	 * Se entregan los premios y se envia un mensaje custom por cada premio.
	 * @param killer
	 * @param victim
	 */
	private static void giveRewards(L2PcInstance killer, L2PcInstance victim)
	{
		for (RewardHolder reward : ConfigData.PVP_REWARDS)
		{
			if (Rnd.get(100) <= reward.getRewardChance())
			{
				killer.sendPacket(new CreatureSay(0, SayType.TELL, "", "Have won " + reward.getRewardCount() + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName()));
				killer.getActingPlayer().getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), killer, victim);
			}
		}
	}
}
