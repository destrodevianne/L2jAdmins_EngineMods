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
package main.engine.stats;

import main.data.PlayerData;
import main.engine.AbstractMods;
import main.util.Util;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.skills.stats.enums.StatsType;

/**
 * @author fissban
 */
public class StatsFake extends AbstractMods
{
	public StatsFake()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public double onStats(StatsType stat, L2Character character, double value)
	{
		if (!Util.areObjectType(L2PcInstance.class, character))
		{
			return value;
		}
		
		if (PlayerData.get((L2PcInstance) character).isFake())
		{
			// bonus generales para todos los fakes
			switch (stat)
			{
				case MAX_MP:
					value *= 10.0;
					break;
				case REG_MP_RATE:
					value *= 10.0;
					break;
				case REG_HP_RATE:
					value *= 10.0;
					break;
				case MAGICAL_DEFENCE:
					value *= 4.0;
					break;
				case PHYSICAL_DEFENCE:
					value *= 4.0;
					break;
				case MAGICAL_SKILL_REUSE:
					value *= 0.8;
					break;
				case PHYSICAL_ATTACK_SPEED:
					value *= 3.0;
					break;
				case MAGICAL_ATTACK_SPEED:
					value *= 3.0;
					break;
				case PHYSICAL_ATTACK:
					value *= 4.5;
					break;
				case MAGICAL_ATTACK:
					value *= 4.5;
					break;
				default:
					break;
			}
		}
		
		return value;
	}
}
