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
package main.engine.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import main.engine.AbstractMods;
import main.util.Util;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.holder.LocationHolder;

/**
 * @author fissban
 */
public class NpcTeleporter extends AbstractMods
{
	private static final int NPC = 14053;
	private static final Map<String, LocationHolder> TELEPORTS = new HashMap<>();
	{
		// 30 missing
		TELEPORTS.put("40", new LocationHolder(121980, -118800, -2574));
		// 50 missing
		TELEPORTS.put("60 TOP", new LocationHolder(174528, 52683, -4369));
		TELEPORTS.put("60 UNDER", new LocationHolder(170327, 53985, -4583));
		TELEPORTS.put("70", new LocationHolder(188191, -74959, -2738));
		// ......missing
	}
	
	public NpcTeleporter()
	{
		registerMod(true);// TODO missing config
		spawnGuards();
	}
	
	@Override
	public void onModState()
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * Se spawnean guardias en las zonas de teleports
	 */
	private void spawnGuards()
	{
		ThreadPoolManager.getInstance().schedule(() ->
		{
			for (LocationHolder loc : TELEPORTS.values())
			{
				addSpawn(60010, loc, true, 0);
				addSpawn(60010, loc, true, 0);
			}
			
		}, 20 * 1000); // 20 seg es hardcode
		
	}
	
	@Override
	public boolean onInteract(L2PcInstance player, L2Character npc)
	{
		if (!Util.areObjectType(L2Npc.class, npc))
		{
			return false;
		}
		
		if (((L2Npc) npc).getId() != NPC)
		{
			return false;
		}
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
		hb.append(Html.HTML_START);
		hb.append(Html.headHtml("TELEPORT MASTER"));
		hb.append("Puedo mostrarte las zonas donde");
		hb.append("los hombres se convierten en <font color=LEVEL>dioses!</font>");
		
		for (String tele : TELEPORTS.keySet())
		{
			hb.append("<table width=280>");
			hb.append("<tr>");
			hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
			hb.append("<td><button value=\"", tele, "\" action=\"bypass -h Engine NpcTeleporter teleport ", tele, "\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
			hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
			hb.append("</tr>");
			hb.append("</table>");
		}
		
		hb.append(Html.HTML_END);
		
		sendHtml(player, (L2Npc) npc, hb);
		
		return true;
	}
	
	@Override
	public void onEvent(L2PcInstance player, L2Character npc, String command)
	{
		if (((L2Npc) npc).getId() != NPC)
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		
		switch (st.nextToken())
		{
			case "teleport":
				String locName = st.nextToken();
				
				if (!TELEPORTS.containsKey(locName))
				{
					// posible bypass....juaz!
					break;
				}
				player.teleToLocation(TELEPORTS.get(locName), true);
				break;
		}
	}
}
