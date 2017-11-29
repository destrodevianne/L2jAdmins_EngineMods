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
package main.engine.community;

import java.util.StringTokenizer;

import main.data.ConfigData;
import main.data.PlayerData;
import main.engine.AbstractMods;
import main.holders.PlayerHolder;
import main.util.Util;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.gameserver.datatables.ExperienceTable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.skills.stats.enums.StatsType;

/**
 * @author fissban
 */
public class FavoriteCommunityBoard extends AbstractMods
{
	public FavoriteCommunityBoard()
	{
		registerMod(ConfigData.ENABLE_BBS_FAVORITE);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				readAllRebirths();
				break;
			
			case END:
				break;
		}
	}
	
	@Override
	public boolean onCommunityBoard(L2PcInstance player, String command)
	{
		// _bbsgetfav
		if (command.startsWith("_bbsgetfav"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			// _bbsgetfav
			st.nextToken();
			// bypass
			String bypass = st.hasMoreTokens() ? st.nextToken() : "main";
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
			hb.append(Html.HTML_START);
			hb.append("<br>");
			hb.append("<center>");
			hb.append(bbsHead(bypass));
			
			switch (bypass)
			{
				case "main":
				{
					hb.append(bbsBodyMain(player));
					break;
				}
				case "maestrias":
				{
					// hb.append(bbsBodyStats(player));
					break;
				}
				case "stats":
				{
					hb.append(bbsBodyPanelStats(player));
					
					// si tenemos mas tokens quiere decir q el player esta sumando puntos a los stats
					if (st.hasMoreTokens())
					{
						if (PlayerData.get(player).getStatsPoints().get() > 0)
						{
							// disminuimos en 1 la cant de puntos del player
							PlayerData.get(player).getStatsPoints().decrementAndGet();
							
							// obtenemos el stat q incrementaremos
							StatsType stat = Enum.valueOf(StatsType.class, st.nextToken());
							// en este punto siempre sabemos que habra otro tolen (add o sub)
							switch (st.nextToken())
							{
								case "add":
									PlayerData.get(player).addCustomStat(stat, 1);
									break;
								case "sub":
									PlayerData.get(player).addCustomStat(stat, -1);
									break;
							}
							
							// salvamos los nuevos valores
							// STATS
							setValueDB(player.getObjectId(), stat.name(), PlayerData.get(player).getCustomStat(stat) + "");
							// POINTS
							setValueDB(player.getObjectId(), "stats", PlayerData.get(player).getStatsPoints().get() + "");
							// actualizamos el cliente con la nueva info.
							player.broadcastUserInfo();
						}
					}
					
					break;
				}
				case "rebirth":
				{
					if (!st.hasMoreTokens())
					{
						hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Deseas renacer???<br>"));
						hb.append("<td><button value=\"RENACER\" action=\"bypass _bbsgetfav;rebirth;yes\" width=75 height=22 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal></td>");
					}
					else
					{
						hb.append(bbsBodyRebirth(player));
					}
					
					break;
				}
			}
			
			hb.append("</center>");
			hb.append(Html.HTML_END);
			
			sendCommunity(player, hb.toString());
			return true;
		}
		
		return false;
	}
	
	@Override
	public double onStats(StatsType stat, L2Character character, double value)
	{
		if (!Util.areObjectType(L2Playable.class, character))
		{
			return value;
		}
		
		switch (stat)
		{
			case STAT_STR:
			case STAT_CON:
			case STAT_DEX:
			case STAT_INT:
			case STAT_WIT:
			case STAT_MEN:
				value += PlayerData.get(character.getActingPlayer()).getCustomStat(stat);
			default:
				break;
		}
		
		return value;
	}
	
	@Override
	public void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		if (!Util.areObjectType(L2MonsterInstance.class, victim) || killer.getActingPlayer() == null)
		{
			return;
		}
		
		if (killer.getLevel() == ExperienceTable.getInstance().getMaxLevel())
		{
			killer.sendMessage("Nivel maximo!");
		}
	}
	
	// HTML ------------------------------------------------------------------------
	
	private String marcButton(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
		if (bypass != null)
		{
			hb.append("<table border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td>", Html.newImage(bypass.equals("main") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
			hb.append("<td>", Html.newImage(bypass.equals("rebirth") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
			hb.append("<td>", Html.newImage(bypass.equals("stats") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
			hb.append("<td>", Html.newImage(bypass.equals("maestrias") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
			hb.append("</tr>");
			hb.append("</table>");
		}
		else
		{
			hb.append(Html.newImage("L2UI.SquareGray", 506, 1));
		}
		return hb.toString();
	}
	
	private String bbsHead(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
		hb.append(marcButton(bypass));
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append(newMenu("MAIN", "main"));
		hb.append(newMenu("RENACER", "rebirth"));
		hb.append(newMenu("STATS", "stats"));
		hb.append(newMenu("MAESTRIAS", "maestrias"));
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(marcButton(bypass));
		
		hb.append("<br>");
		return hb.toString();
	}
	
	private String bbsBodyRebirth(L2PcInstance player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
		// chequeamos la cantidad de rebirths del pj y evitamos que haga mas del deseado.
		if (PlayerData.get(player).getRebirth() >= ConfigData.MAX_REBIRTH)
		{
			hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Ya no puedes renacer mas veces!<br>"));
			hb.append("Recuerda que el maximo de rebirths es de ", ConfigData.MAX_REBIRTH);
			return hb.toString();
		}
		// chequeamos que el personaje este en el maximo level.
		if (player.getLevel() < ExperienceTable.getInstance().getMaxLevel() - 1)
		{
			hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Aun no has logrado llegar al nivel maxoimo!<br>"));
			hb.append("Recuerda que el nivel para renacer es ", ExperienceTable.getInstance().getMaxLevel() - 1);
			return hb.toString();
		}
		
		// si no tenemos mas chequeos hacemos el rebirth xD
		
		player.removeExpAndSp(player.getExp() - ExperienceTable.getInstance().getExpForLevel(ConfigData.LVL_REBIRTH), 0);
		// incrementamos la cantidad de rebirths del pj
		PlayerData.get(player).increaseRebirth();
		// salvamos el valor en la DB
		setValueDB(player.getObjectId(), "rebirth", PlayerData.get(player).getRebirth() + "");
		setValueDB(player.getObjectId(), "maestrias", PlayerData.get(player).getMaestriasPoints().addAndGet(ConfigData.MASTERY_POINT_PER_REBIRTH) + "");
		// stats
		setValueDB(player.getObjectId(), "stats", PlayerData.get(player).getStatsPoints().addAndGet(ConfigData.STAT_POINT_PER_REBIRTH) + "");
		
		setValueDB(player.getObjectId(), "STAT_STR", 0 + "");
		setValueDB(player.getObjectId(), "STAT_CON", 0 + "");
		setValueDB(player.getObjectId(), "STAT_DEX", 0 + "");
		setValueDB(player.getObjectId(), "STAT_INT", 0 + "");
		setValueDB(player.getObjectId(), "STAT_WIT", 0 + "");
		setValueDB(player.getObjectId(), "STAT_MEN", 0 + "");
		
		// enviamos mensaje sobre la nueva actualizacion
		hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Felicitaciones, has conseguido el rebirth con exito!<br>"));
		hb.append("No olvides sumar tus puntos y mejorar tus maestrias<br>");
		// TODO podriamos mostrar los puntos q ganaron no?
		hb.append("");
		hb.append("");
		hb.append("");
		return hb.toString();
	}
	
	public String bbsBodyMain(L2PcInstance player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
		hb.append("<br>");
		hb.append("<center>");
		hb.append("Bienvenido ", Html.newFontColor("LEVEL", player.getName()), " al sistema de rebirth.<br>");
		hb.append("Si has logrado llegar al nivel ", Html.newFontColor("LEVEL", ExperienceTable.getInstance().getMaxLevel()), ", estas listo para poder renacer<br>");
		hb.append("y convertirte en un guerrero mas poderoso....<br>");
		hb.append("quizas hasta podrias alcanzar el poder de un dios!<br>");
		hb.append("<br>");
		hb.append("Actualmente tienes ", Html.newFontColor("LEVEL", PlayerData.get(player).getRebirth()), " rebirths y podras renacer ", Html.newFontColor("LEVEL", ConfigData.MAX_REBIRTH), " veces.<br>");
		hb.append("<br>");
		hb.append("Con cada rebirth ganaras:<br>");
		hb.append("* ", Html.newFontColor("LEVEL", ConfigData.STAT_POINT_PER_REBIRTH), " que podras sumarlos a los stas que gustes.<br>");
		hb.append("* ", Html.newFontColor("LEVEL", ConfigData.MASTERY_POINT_PER_REBIRTH), " que podras mejorar tu arbol de maestrias.<br>");
		return hb.toString();
	}
	
	private static String bbsBodyPanelStats(L2PcInstance player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
		
		hb.append("<br>");
		
		hb.append("<table bgcolor=000000 height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
		hb.append("<td width=250 align=center height=22><button value=\"EXTRA POINTS: ", PlayerData.get(player).getStatsPoints().get(), "\" width=250 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<br>");
		
		hb.append("<table height=22 width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=91 align=center>", Html.newFontColor("FF8000", "STAT"), "</td>");
		hb.append("<td width=125 align=center>", Html.newFontColor("FF8000", "POINTS"), "</td>");
		hb.append("<td width=66 align=center>", Html.newFontColor("FF8000", "ACTION"), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<br>");
		
		// STR ------------------------------------------------------------------------------------------------
		hb.append("<table width=282 height=22 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
		hb.append("<td width=75 align=center height=22><button value=STR width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getSTR(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_STR;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "P Atk: "), player.getStat().getPAtk(null), "</td></tr>");
		hb.append("</table>");
		
		// DEX ------------------------------------------------------------------------------------------------
		hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");;
		hb.append("<td width=75 align=center height=22><button value=DEX width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getDEX(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_DEX;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Atk Spd: "), player.getStat().getPAtkSpd(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Accuracy: "), player.getStat().getAccuracy(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Evasion: "), player.getStat().getEvasionRate(null), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "P Critical Rate: "), player.getStat().getCriticalHit(null, null), "</td></tr>");
		hb.append("</table>");
		
		// CON ------------------------------------------------------------------------------------------------
		hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");;
		hb.append("<td width=75 align=center height=22><button value=CON width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getCON(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_CON;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxHp: "), player.getStat().getMaxHp(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxCp: "), player.getStat().getMaxCp(), "</td></tr>");
		hb.append("</table>");
		
		// INT ------------------------------------------------------------------------------------------------
		hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");;
		hb.append("<td width=75 align=center height=22><button value=INT width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getINT(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_INT;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Atk: "), player.getStat().getMAtk(null, null), "</td></tr>");
		hb.append("</table>");
		
		// WIT ------------------------------------------------------------------------------------------------
		hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");;
		hb.append("<td width=75 align=center height=22><button value=WIT width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getWIT(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_WIT;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Spd: "), player.getStat().getMAtkSpd(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Critical Rate: "), player.getStat().getMCriticalHit(null, null), "</td></tr>");
		hb.append("</table>");
		
		// MEN ------------------------------------------------------------------------------------------------
		hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
		hb.append("<td width=75 align=center height=22><button value=MEN width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=125 align=center height=22><button value=", player.getStat().getMEN(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_MEN;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxMp: "), player.getStat().getMaxMp(), "</td></tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	// MISC ------------------------------------------------------------------------
	private static String newMenu(String butonName, String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append("<td><button value=\"", butonName, "\" action=\"bypass _bbsgetfav;", bypass, "\" width=100 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_22></td>");
		return hb.toString();
	}
	
	private void readAllRebirths()
	{
		for (PlayerHolder ph : PlayerData.getAllPlayers())
		{
			// obtenemos la cantidad de rebirths de cada pj en la DB
			String rebirthCount = getValueDB(ph.getObjectId(), "rebirth");
			// Don't has value in db
			if (rebirthCount == null)
			{
				continue;
			}
			
			int rebirth = Integer.parseInt(rebirthCount);
			
			if (rebirth == 0)
			{
				continue;
			}
			
			// salvamos la cantidad de rebirths en la memoria
			PlayerData.get(ph.getObjectId()).setRebirth(rebirth);
			try
			{
				// obtenemos y salvamos la cantidad de puntos de maestrias a repartir
				String mCount = getValueDB(ph.getObjectId(), "maestrias");
				PlayerData.get(ph.getObjectId()).getMaestriasPoints().set(Integer.parseInt(mCount));
				
				// obtenemos y salvamos la cantidad de puntos de maestrias a repartir
				String sCount = getValueDB(ph.getObjectId(), "stats");
				PlayerData.get(ph.getObjectId()).getStatsPoints().set(Integer.parseInt(sCount));
				
				// obtenemos los puntos q se agregaron a cada stat
				int stat_str = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_STR"));
				int stat_con = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_CON"));
				int stat_dex = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_DEX"));
				int stat_int = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_INT"));
				int stat_wit = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_WIT"));
				int stat_men = Integer.parseInt(getValueDB(ph.getObjectId(), "STAT_MEN"));
				
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_STR, stat_str);
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_CON, stat_con);
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_DEX, stat_dex);
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_INT, stat_int);
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_WIT, stat_wit);
				PlayerData.get(ph.getObjectId()).addCustomStat(StatsType.STAT_MEN, stat_men);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
