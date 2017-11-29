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
package main.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import main.enums.ExpSpType;
import net.sf.l2j.gameserver.model.actor.L2Attackable;
import net.sf.l2j.gameserver.model.actor.L2Attackable.AggroInfo;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Playable;
import net.sf.l2j.gameserver.model.actor.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.party.Party;
import net.sf.l2j.gameserver.model.skills.stats.enums.StatsType;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

/**
 * @author fissban
 */
public class NpcExpInstance
{
	private Map<ExpSpType, Double> _expSettings = new HashMap<>();
	{
		_expSettings.put(ExpSpType.EXP, 1.0);
		_expSettings.put(ExpSpType.SP, 1.0);
	}
	
	public NpcExpInstance()
	{
		//
	}
	
	public void increaseRate(ExpSpType type, double bonus)
	{
		double oldValue = _expSettings.get(type);
		_expSettings.put(type, oldValue + bonus - 1);
	}
	
	public boolean hasSettings()
	{
		for (Double value : _expSettings.values())
		{
			if (value > 1.0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void init(L2Attackable npc, L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		Map<L2Character, RewardInfo> rewards = new ConcurrentHashMap<>();
		
		try
		{
			if (npc.noHasTarget())
			{
				return;
			}
			
			int damage;
			L2Character attacker, ddealer;
			
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			
			// Go through the _aggroList of the L2Attackable
			for (final AggroInfo info : npc.getAggroList().values())
			{
				if (info == null)
				{
					continue;
				}
				
				// Get the L2Character corresponding to this attacker
				attacker = info.getAttacker();
				
				// Get damages done by this attacker
				damage = info.getDamage();
				
				// Prevent unwanted behavior
				if (damage > 1)
				{
					if (attacker instanceof L2SummonInstance || attacker instanceof L2PetInstance)
					{
						ddealer = attacker.getActingPlayer();
					}
					else
					{
						ddealer = attacker;
					}
					
					// TODO en C4 no importa la distancia...si hicistes daño te llevas exp.
					// if (!Util.checkIfInRange(1600, this, ddealer, true))
					// {
					// continue;
					// }
					
					// Calculate real damages (Summoners should get own damage plus summon's damage)
					RewardInfo reward = rewards.get(ddealer);
					
					if (reward == null)
					{
						reward = new RewardInfo(ddealer, damage);
					}
					else
					{
						reward.addDamage(damage);
					}
					
					rewards.put(ddealer, reward);
					
					// set max dealer owner pet or summon or player
					maxDealer = ddealer.getActingPlayer();
					
					if (maxDealer != null && reward._dmg > maxDamage)
					{
						maxDamage = reward._dmg;
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			npc.doItemDrop(maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker);
			
			if (!npc.getMustRewardExpSP())
			{
				return;
			}
			
			if (!rewards.isEmpty())
			{
				Party attackerParty;
				
				long exp;
				int levelDiff;
				int partyDmg;
				int partyLvl;
				float partyMul;
				float penalty;
				
				RewardInfo reward2;
				int sp;
				int[] tmp;
				
				for (final RewardInfo reward : rewards.values())
				{
					if (reward == null)
					{
						continue;
					}
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward._attacker;
					
					// Total amount of damage done
					damage = reward._dmg;
					
					if (attacker instanceof L2Playable)
					{
						attackerParty = attacker.getActingPlayer().getParty();
					}
					else
					{
						return;
					}
					
					// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
					if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getPet() instanceof L2SummonInstance)
					{
						penalty = ((L2SummonInstance) ((L2PcInstance) attacker).getPet()).getExpPenalty();
					}
					
					// We must avoid "over damage", if any
					if (damage > npc.getStat().getMaxHp())
					{
						damage = npc.getStat().getMaxHp();
					}
					
					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(npc))
						{
							// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							levelDiff = attacker.getLevel() - npc.getLevel();
							
							tmp = calculateExpAndSp(npc, levelDiff, damage);
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = tmp[1];
							
							// Check for an over-hit enabled strike
							if (attacker instanceof L2PcInstance)
							{
								final L2PcInstance player = (L2PcInstance) attacker;
								if (npc.isOverhit() && attacker == npc.getOverhitAttacker())
								{
									player.sendPacket(SystemMessage.OVER_HIT);
									exp += npc.calculateOverhitExp(exp);
								}
							}
							
							// In the case of pets (L2PetInstance) they only gain experience if they are to kill the mobs.
							// TODO: Here it will be nice if:
							// - also owner pet owner should get exp reward only form own dmg;
							// - if other pet done dmg, get exp depend on dmg;
							if (lastAttacker instanceof L2PetInstance)
							{
								lastAttacker.addExpAndSp(Math.round(lastAttacker.calcStat(StatsType.EXPSP_RATE, exp, null, null)), (int) lastAttacker.calcStat(StatsType.EXPSP_RATE, sp, null, null));
							}
							else
							{
								attacker.addExpAndSp(Math.round(attacker.calcStat(StatsType.EXPSP_RATE, exp, null, null)), (int) attacker.calcStat(StatsType.EXPSP_RATE, sp, null, null));
							}
						}
					}
					else
					{
						// share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all L2Character that can be rewarded in the party
						final List<L2PcInstance> rewardedMembers = new ArrayList<>();
						
						// Go through all L2PcInstance in the party
						for (final L2PcInstance player : attackerParty.getPartyMembers())
						{
							if (player == null || player.isDead())
							{
								continue;
							}
							
							// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
							reward2 = rewards.get(player);
							
							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								// if (Util.checkIfInRange(1600, this, pl, true))
								// {
								// Add L2PcInstance damages to party damages
								partyDmg += reward2._dmg;
								rewardedMembers.add(player);
								
								if (player.getLevel() > partyLvl)
								{
									partyLvl = player.getLevel();
								}
								// }
								
								// Remove the L2PcInstance from the L2Attackable rewards
								rewards.remove(player);
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded if it's not dead
								// and in range of the monster.
								if (Util.checkIfInRange(1600, npc, player, true))
								{
									rewardedMembers.add(player);
									
									if (player.getLevel() > partyLvl)
									{
										partyLvl = player.getLevel();
									}
								}
							}
							
							// TODO: We need to invent method to:
							// if pet owner_exp_taken == 0
							// add pet to rewardedMembers (or give exp here) if pet done dmg - and give correct exp reward depend on pet dmg;
							// and dont give exp reward from pet dmg to owner/party members;
							// and give exp reward from owner dmg to owner/party members; (exclude pet!)
							// for clarity: if both (owner, pet) done dmg, give reward to both. According to rules mentioned above.
							// else
							// add owner to rewardedMembers (doesn't matter if pet/owner done damage)
							// note: actually we apply penalty in Party#distributeXpAndSp;
							// add exp reward to pet, depend on owner~penalty (exp reward*owner_exp_taken);
							// for clarity: owner get reward with penalty from distributeXpAndSp, pet need to receive part for him;
							// eg. pet with 0.10 penalty and exp to divide for them 1000: pet get 1000*0.10, owner get 1000*0.90;
							
							// Until that time, we must remove reward here, for owner with pet.
							// Current way is definitely wrong.
							final L2Summon summon = player.getPet();
							if (summon != null && summon instanceof L2PetInstance)
							{
								rewardedMembers.remove(summon.getOwner());
							}
						}
						
						// If the party didn't killed this L2Attackable alone
						if (partyDmg < npc.getStat().getMaxHp())
						{
							partyMul = (float) partyDmg / npc.getStat().getMaxHp();
						}
						
						// Avoid "over damage"
						if (partyDmg > npc.getStat().getMaxHp())
						{
							partyDmg = npc.getStat().getMaxHp();
						}
						
						// Calculate the level difference between Party and L2Attackable
						levelDiff = partyLvl - npc.getLevel();
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(npc, levelDiff, partyDmg);
						exp = tmp[0];
						sp = tmp[1];
						
						exp *= partyMul;
						sp *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof L2PcInstance)
						{
							final L2PcInstance player = (L2PcInstance) attacker;
							if (npc.isOverhit() && attacker == npc.getOverhitAttacker())
							{
								player.sendPacket(SystemMessage.OVER_HIT);
								exp += npc.calculateOverhitExp(exp);
							}
						}
						
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0 && !rewardedMembers.isEmpty())
						{
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl);
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private int[] calculateExpAndSp(L2Attackable npc, int diff, int damage)
	{
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		xp = (double) npc.getExpReward() * damage / npc.getStat().getMaxHp();
		sp = (double) npc.getSpReward() * damage / npc.getStat().getMaxHp();
		
		if (diff > 5)
		{
			final double pow = Math.pow((double) 5 / 6, diff - 5);
			xp = xp * pow;
			sp = sp * pow;
		}
		
		xp *= _expSettings.get(ExpSpType.EXP);
		sp *= _expSettings.get(ExpSpType.SP);
		
		if (xp <= 0)
		{
			xp = 0;
			sp = 0;
		}
		else if (sp <= 0)
		{
			sp = 0;
		}
		
		final int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		
		return tmp;
	}
	
	protected final class RewardInfo
	{
		protected L2Character _attacker;
		protected int _dmg = 0;
		
		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}
		
		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			
			if (obj instanceof RewardInfo)
			{
				return ((RewardInfo) obj)._attacker.getObjectId() == _attacker.getObjectId();
			}
			
			return false;
		}
	}
}
