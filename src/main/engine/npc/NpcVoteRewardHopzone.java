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

import main.data.ConfigData;
import main.engine.AbstractMods;
import main.engine.mods.VoteReward;
import main.util.Util;
import main.util.UtilInventory;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.clientpackets.Say2.SayType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

/**
 * @author fissban
 */
public class NpcVoteRewardHopzone extends AbstractMods
{
	private static final int NPC = 14054;
	// player q esta votando
	private static L2PcInstance _player = null;
	private static int _votes = 0;
	
	public NpcVoteRewardHopzone()
	{
		registerMod(ConfigData.ENABLE_VoteRewardIndivualHopzone);
	}
	
	@Override
	public void onModState()
	{
		//
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
		hb.append(Html.headHtml("VOTE REWARD HOPZONE"));
		hb.append("<br>");
		hb.append("<center>");
		hb.append("Bienvenido ", Html.newFontColor("LEVEL", player.getName()), "<br>");
		
		if (!checkLastVote(player))
		{
			hb.append("Lo siento, aun no pasaron las ", Html.newFontColor("LEVEL", "12hs"), "<br>");
			hb.append("desde tu ultimo voto, intenta mas tarde.<br>");
		}
		else
		{
			hb.append("Aqui podras votar por nuestro server<br>");
			hb.append("y obtener una buena recompenza por ello.<br>");
			if (_player == null)
			{
				hb.append("Actualmente nadie esta votando<br>");
				hb.append("No esperes mas, ", Html.newFontColor("LEVEL", "vota por nosotros!"), "<br>");
				
				hb.append("<table width=280>");
				hb.append("<tr>");
				hb.append("<td align=center>", Html.newImage(L2UI.bbs_folder, 32, 32), "</td>");
				hb.append("<td><button value=\"Vote\" action=\"bypass -h Engine " + NPC + " ", NpcVoteRewardHopzone.class.getSimpleName(), " vote\" width=216 height=32 back=", L2UI_CH3.refinegrade3_21, " fore=", L2UI_CH3.refinegrade3_21, "></td>");
				hb.append("<td align=center>", Html.newImage(L2UI.bbs_folder, 32, 32), "</td>");
				hb.append("</tr>");
				hb.append("</table>");
			}
			else
			{
				hb.append("Actualmente se encuentra ", Html.newFontColor("LEVEL", _player.getName()), " votando<br>");
				hb.append("Solo espera un momento y ya sera tu turno<br>");
			}
		}
		
		hb.append("</center>");
		hb.append(Html.HTML_END);
		
		sendHtml(player, (L2Npc) npc, hb);
		
		return true;
	}
	
	private boolean checkLastVote(L2PcInstance player)
	{
		// prevenimos que un player q ya voto nos bypasee
		String vote = getValueDB(player, "lastVote");
		if (vote != null)
		{
			long lastVote = Long.parseLong(vote);
			
			// chequeamos si ya transcurrio el tiempo para volver a votar
			if (lastVote + ConfigData.INDIVIDUAL_VOTE_TIME_CAN_VOTE * 3600000 > System.currentTimeMillis())
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void onEvent(L2PcInstance player, L2Npc npc, String command)
	{
		if (npc.getId() != NPC)
		{
			return;
		}
		
		// anti bypass
		if (_player != null)
		{
			return;
		}
		
		// prevenimos que un player q ya voto nos bypasee
		if (!checkLastVote(player))
		{
			return;
		}
		
		// inicializamos los votes.
		_votes = -1;
		
		if (command.equals("vote"))
		{
			try
			{
				_votes = VoteReward.getVotesHopzone();
				HtmlBuilder hb = new HtmlBuilder();
				hb.append(Html.HTML_START);
				hb.append(Html.headHtml("VOTE REWARD HOPZONE"));
				hb.append("<br>");
				hb.append("<br>");
				hb.append("<center>");
				
				if (_votes == -1)
				{
					// no se pudieron obtener los votos
					hb.append("No se pudieron obtener los votos,<br>intenta mas tarde");
				}
				else
				{
					_player = player;
					
					hb.append("Tienes ", Html.newFontColor("LEVEL", ConfigData.INDIVIDUAL_VOTE_TIME_VOTE), " segundos para votar");
					
					startTimer("waitVote", ConfigData.INDIVIDUAL_VOTE_TIME_VOTE * 1000, null, player, false);
				}
				
				hb.append("</center>");
				hb.append(Html.HTML_END);
				sendHtml(player, npc, hb);
			}
			catch (Exception e)
			{
				_votes = 0;
				_player = null;
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onTimer(String timerName, L2Npc npc, L2PcInstance player)
	{
		switch (timerName)
		{
			case "waitVote":
				if (VoteReward.getVotesHopzone() > _votes)
				{
					player.sendPacket(new CreatureSay(0, SayType.TELL, "", "felicidades, tu voto se registro con exito!"));
					UtilInventory.giveItems(player, ConfigData.INDIVIDUAL_VOTE_REWARD.getRewardId(), ConfigData.INDIVIDUAL_VOTE_REWARD.getRewardCount(), 0);
					setValueDB(player, "lastVote", System.currentTimeMillis() + "");
				}
				else
				{
					player.sendPacket(new CreatureSay(0, SayType.TELL, "", "No se pudo verificar tu voto, intenta mas tarde"));
				}
				
				_player = null;
				break;
		}
	}
}
