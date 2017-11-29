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

import main.engine.AbstractMods;
import main.util.Util;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.skills.stats.enums.StatsType;

/**
 * @author fissban
 */
public class StatsNpc extends AbstractMods
{
	public StatsNpc()
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
		if (!Util.areObjectType(L2MonsterInstance.class, character))
		{
			return value;
		}
		
		// bonus generales para todos.
		switch (stat)
		{
			case PHYSICAL_DEFENCE:
				value /= 1.0;
				break;
			case MAGICAL_DEFENCE:
				value /= 1.0;
				break;
			default:
				break;
		}
		
		return value;
	}
}
