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
package main;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.data.ConfigData;
import main.data.IconData;
import main.data.ModsData;
import main.data.PlayerData;
import main.data.SchemeBuffData;
import main.data.SkillData;
import main.engine.AbstractMods;
import main.engine.community.ClanCommunityBoard;
import main.engine.community.FavoriteCommunityBoard;
import main.engine.community.HomeComunityBoard;
import main.engine.community.MemoCommunityBoard;
import main.engine.community.RegionComunityBoard;
import main.engine.events.BonusWeekend;
import main.engine.events.Champions;
import main.engine.events.CityElpys;
import main.engine.events.RandomBossSpawn;
import main.engine.mods.AnnounceKillBoss;
import main.engine.mods.AntiBot;
import main.engine.mods.ColorAccordingAmountPvPorPk;
import main.engine.mods.EnchantAbnormalEffectArmor;
import main.engine.mods.NewCharacterCreated;
import main.engine.mods.PvpReward;
import main.engine.mods.SellBuffs;
import main.engine.mods.SpreeKills;
import main.engine.mods.SubClassAcumulatives;
import main.engine.mods.SystemAio;
import main.engine.mods.SystemVip;
import main.engine.mods.VoteReward;
import main.engine.npc.NpcBufferScheme;
import main.engine.npc.NpcRanking;
import main.engine.npc.NpcTeleporter;
import main.engine.npc.NpcVoteRewardHopzone;
import main.engine.npc.NpcVoteRewardNetwork;
import main.engine.npc.NpcVoteRewardTopzone;
import main.engine.stats.StatsFake;
import main.engine.stats.StatsNpc;
import main.engine.stats.StatsPlayer;
import main.instances.NpcDropsInstance;
import main.instances.NpcExpInstance;
import main.util.Util;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.skills.stats.enums.StatsType;
import net.sf.l2j.gameserver.model.zone.Zone;

/**
 * @author fissban
 */
public class EngineModsManager
{
	private static final Logger LOG = Logger.getLogger(AbstractMods.class.getName());
	
	private static final Map<Integer, AbstractMods> ENGINES_MODS = new LinkedHashMap<>();
	
	public EngineModsManager()
	{
		//
	}
	
	public static void init()
	{
		//@formatter:off
		LOG.config(Util.SEPARATOR);
		try{    loadCredits();          }catch(Exception e){e.printStackTrace();}
		LOG.config(Util.SEPARATOR);
		try{    ModsData.load();        }catch(Exception e){e.printStackTrace();}
		try{    PlayerData.load();      }catch(Exception e){e.printStackTrace();}
		try{    IconData.load();        }catch(Exception e){e.printStackTrace();}
		try{    SkillData.load();       }catch(Exception e){e.printStackTrace();}
		try{    ConfigData.load();      }catch(Exception e){e.printStackTrace();}
		try{    SchemeBuffData.load();  }catch(Exception e){e.printStackTrace();}
		LOG.config(Util.SEPARATOR);
		loadModsAndEvents();
		//@formatter:on
	}
	
	private static void loadCredits()
	{
		LOG.config("");
		LOG.config("L2J_EngineMods designed by Fissban");
		LOG.config("    http://l2devsadmins.net       ");
		LOG.config("");
	}
	
	/**
	 * Cargamos todos los mods y eventos.
	 */
	private static void loadModsAndEvents()
	{
		try
		{
			// mods
			ColorAccordingAmountPvPorPk.class.newInstance();
			EnchantAbnormalEffectArmor.class.newInstance();
			SpreeKills.class.newInstance();
			SubClassAcumulatives.class.newInstance();
			PvpReward.class.newInstance();
			AnnounceKillBoss.class.newInstance();
			SellBuffs.class.newInstance();
			VoteReward.class.newInstance();
			AntiBot.class.newInstance();
			NewCharacterCreated.class.newInstance();
			SystemAio.class.newInstance();
			SystemVip.class.newInstance();
			// events
			BonusWeekend.class.newInstance();
			Champions.class.newInstance();
			RandomBossSpawn.class.newInstance();
			CityElpys.class.newInstance();
			// npc
			NpcRanking.class.newInstance();
			NpcTeleporter.class.newInstance();
			NpcBufferScheme.class.newInstance();
			NpcVoteRewardHopzone.class.newInstance();
			NpcVoteRewardNetwork.class.newInstance();
			NpcVoteRewardTopzone.class.newInstance();
			// community
			RegionComunityBoard.class.newInstance();
			HomeComunityBoard.class.newInstance();
			FavoriteCommunityBoard.class.newInstance();
			ClanCommunityBoard.class.newInstance();
			MemoCommunityBoard.class.newInstance();
			// stats
			StatsFake.class.newInstance();
			StatsPlayer.class.newInstance();
			StatsNpc.class.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void registerMod(AbstractMods type)
	{
		ENGINES_MODS.put(type.hashCode(), type);
	}
	
	public static Collection<AbstractMods> getAllMods()
	{
		return ENGINES_MODS.values();
	}
	
	public static AbstractMods getMod(int type)
	{
		return ENGINES_MODS.get(type);
	}
	
	/** MISC ---------------------------------------------------------------------------------------------- */
	
	/** LISTENERS ----------------------------------------------------------------------------------------- */
	public static synchronized boolean onCommunityBoard(L2PcInstance player, String command)
	{
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				if (mod.onCommunityBoard(player, command))
				{
					return true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		} ;
		
		return false;
	}
	
	public static synchronized void onShutDown()
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onShutDown();
				mod.endMod();
				mod.cancelScheduledState();
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized boolean onExitWorld(L2PcInstance player)
	{
		boolean exitPlayer = false;
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			try
			{
				if (!mod.isStarting())
				{
					continue;
				}
				if (mod.onExitWorld(player))
				{
					exitPlayer = true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return exitPlayer;
	}
	
	public static synchronized boolean onNpcExpSp(L2Attackable npc, L2Character character)
	{
		if (character == null)
		{
			return false;
		}
		
		L2PcInstance killer = character.getActingPlayer();
		
		if (killer == null)
		{
			return false;
		}
		
		NpcExpInstance instance = new NpcExpInstance();
		
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			try
			{
				if (mod.isStarting())
				{
					mod.onNpcExpSp(killer, npc, instance);
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (instance.hasSettings())
		{
			instance.init(npc, character);
			return true;
		}
		
		return false;
	}
	
	public static synchronized boolean onNpcDrop(L2Attackable npc, L2Character character)
	{
		if (character == null)
		{
			return false;
		}
		
		L2PcInstance killer = character.getActingPlayer();
		
		if (killer == null)
		{
			return false;
		}
		
		NpcDropsInstance instance = new NpcDropsInstance();
		
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			try
			{
				if (mod.isStarting())
				{
					mod.onNpcDrop(killer, npc, instance);
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (instance.hasSettings())
		{
			instance.init(npc, character);
			return true;
		}
		
		return false;
	}
	
	public static synchronized void onEnterZone(L2Character player, Zone zone)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnterZone(player, zone);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onExitZone(L2Character player, Zone zone)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onExitZone(player, zone);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onCreateCharacter(L2PcInstance player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onCreateCharacter(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized boolean onVoiced(L2PcInstance player, String chat)
	{
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (chat.startsWith("admin_"))
				{
					if (player.getAccessLevel() < 1)
					{
						return false;
					}
					
					if (mod.onAdminCommand(player, chat.replace("admin_", "")))
					{
						return true;
					}
				}
				else if (chat.startsWith("."))
				{
					if (mod.onVoicedCommand(player, chat.replace(".", "")))
					{
						return true;
					}
				}
				else
				{
					if (mod.onChat(player, chat))
					{
						return true;
					}
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public static synchronized boolean onInteract(L2PcInstance player, L2Character character)
	{
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			try
			{
				if (!mod.onInteract(player, character))
				{
					continue;
				}
				else
				{
					return true;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Todos los bypass tienen que tener el formato "bypass -h Engine modName bypassName", pero al mod solo llegara "bypassName".
	 * @param player
	 * @param npc
	 * @param command
	 */
	public static synchronized void onEvent(L2PcInstance player, String command)
	{
		String[] params = command.split(" ");
		
		try
		{
			int npcId = Integer.parseInt(params[0]);
			String modName = params[1];
			String event = command.replaceFirst(npcId + " ", "").replaceFirst(modName + " ", "");
			
			ENGINES_MODS.values().stream().filter(mod -> modName.equalsIgnoreCase(mod.getClass().getSimpleName()) && mod.isStarting()).forEach(mod ->
			{
				L2Npc npc = null;
				
				if (npcId != 0)
				{
					npc = player.getKnownList().getKnownTypeInRadius(L2Npc.class, L2Npc.INTERACTION_DISTANCE).stream().filter(n -> n.getId() == npcId).findFirst().orElse(null);
					
					if (npc == null)
					{
						return;
					}
				}
				
				try
				{
					mod.onEvent(player, npc, event);
				}
				catch (Exception e)
				{
					LOG.log(Level.SEVERE, e.getMessage());
					e.printStackTrace();
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static synchronized String onSeeNpcTitle(int objectId)
	{
		String title = null;
		
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			try
			{
				String aux = mod.onSeeNpcTitle(objectId);
				if (aux != null)
				{
					title = aux;
				}
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return title;
	}
	
	public static synchronized void onSpawn(L2Npc obj)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onSpawn(obj);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onEnterWorld(L2PcInstance player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnterWorld(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onKill(L2Character killer, L2Character victim, boolean isPet)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onKill(killer, victim, isPet);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onDeath(L2Character player)
	{
		try
		{
			ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod -> mod.onDeath(player));
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static synchronized void onEnchant(L2Character player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEnchant(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onEquip(L2Character player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onEquip(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized void onUnequip(L2Character player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onUnequip(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	public static synchronized boolean onRestoreSkills(L2PcInstance player)
	{
		ENGINES_MODS.values().stream().filter(mod -> mod.isStarting()).forEach(mod ->
		{
			try
			{
				mod.onRestoreSkills(player);
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		});
		
		return false;
	}
	
	public static synchronized double onStats(StatsType stat, L2Character character, double value)
	{
		for (AbstractMods mod : ENGINES_MODS.values())
		{
			if (!mod.isStarting())
			{
				continue;
			}
			
			if (character == null)
			{
				continue;
			}
			
			try
			{
				value += mod.onStats(stat, character, value) - value;
			}
			catch (Exception e)
			{
				LOG.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
		}
		
		return value;
	}
}
