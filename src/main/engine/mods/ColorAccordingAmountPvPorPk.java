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

import java.util.Map.Entry;

import main.data.ConfigData;
import main.engine.AbstractMods;
import main.util.Util;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * Here define a color for the title and the name according to:<br>
 * <li>Amount pvp</li><br>
 * <li>Amount pk</li><br>
 * @author fissban
 */
public class ColorAccordingAmountPvPorPk extends AbstractMods
{
	/**
	 * Constructor
	 */
	public ColorAccordingAmountPvPorPk()
	{
		registerMod(ConfigData.ENABLE_ColorAccordingAmountPvPorPk);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		if (!Util.areObjectType(L2PcInstance.class, killer, victim))
		{
			return;
		}
		
		checkColorPlayer(killer.getActingPlayer());
	}
	
	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		checkColorPlayer(player);
	}
	
	private static void checkColorPlayer(L2PcInstance activeChar)
	{
		// ignore gms
		if (activeChar.isGM())
		{
			return;
		}
		
		// init vars
		String colorPvp = "";
		String colorPk = "";
		
		// the amount of PvP character is checked to see what color you assign
		for (Entry<Integer, String> pvp : ConfigData.PVP_COLOR_NAME.entrySet())
		{
			if (activeChar.getPvpKills() >= pvp.getKey())
			{
				colorPvp = pvp.getValue();
			}
		}
		// the amount of Pk character is checked to see what color you assign
		for (Entry<Integer, String> pk : ConfigData.PK_COLOR_TITLE.entrySet())
		{
			if (activeChar.getPkKills() >= pk.getKey())
			{
				colorPk = pk.getValue();
			}
		}
		
		// set color name
		if (!colorPvp.equals(""))
		{
			activeChar.setNameColor(Integer.decode("0x" + colorPvp));
		}
		
		// set title name
		if (!colorPk.equals(""))
		{
			activeChar.setTitleColor(Integer.decode("0x" + colorPk));
		}
		
		// update info
		activeChar.broadcastUserInfo();
	}
}
