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

import main.data.ConfigData;
import main.engine.AbstractMods;
import main.util.Util;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.model.actor.L2Character;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.holder.ArmorSetHolder;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.items.enums.ParpedollType;
import net.sf.l2j.gameserver.model.items.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

/**
 * Class responsible for giving the character a "custom" effect by having all their set enchanted to xxx
 * @author fissban
 */
public class EnchantAbnormalEffectArmor extends AbstractMods
{
	/**
	 * Constructor
	 */
	public EnchantAbnormalEffectArmor()
	{
		registerMod(ConfigData.ENABLE_EnchantAbnormalEffectArmor);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onEnchant(L2Character player)
	{
		checkSetEffect(player);
	}
	
	@Override
	public void onEquip(L2Character player)
	{
		checkSetEffect(player);
	}
	
	@Override
	public void onUnequip(L2Character player)
	{
		checkSetEffect(player);
	}
	
	@Override
	public boolean onExitWorld(L2PcInstance player)
	{
		cancelTimer("customEffectSkill", null, player);
		
		return super.onExitWorld(player);
	}
	
	@Override
	public void onTimer(String timerName, L2Npc npc, L2PcInstance player)
	{
		switch (timerName)
		{
			case "customEffectSkill":
				
				if (player != null)
				{
					player.broadcastPacket(new MagicSkillUse(player, player, 4326, 1, 1000, 1000));
				}
				break;
		}
	}
	
	/** MISC --------------------------------------------------------------------------------------------- */
	
	private void checkSetEffect(L2Character character)
	{
		if (!Util.areObjectType(L2PcInstance.class, character))
		{
			return;
		}
		
		L2PcInstance player = (L2PcInstance) character;
		
		// We review the positions of the set of the character.
		if (checkItems(player))
		{
			startTimer("customEffectSkill", 2000, null, player, true);
		}
		else
		{
			// if the character has the effect would Cancelled
			cancelTimer("customEffectSkill", null, player);
		}
	}
	
	/**
	 * It checks the character:<br>
	 * <li>Keep all equipment + ENCHANT_EFFECT_LVL except the coat and jewelry</li><br>
	 * <li>You have equipped a complete set according to "ArmorSetsTable"</li> <br>
	 * @param player
	 * @param paperdoll
	 * @return
	 */
	private boolean checkItems(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		// Checks if player is wearing a chest item
		final ItemInstance chestItem = inv.getPaperdollItem(ParpedollType.CHEST);
		if (chestItem == null)
		{
			return false;
		}
		
		// checks if there is armorset for chest item that player worns
		final ArmorSetHolder armorSet = ArmorSetsTable.getInstance().getArmorSets(chestItem.getId());
		if (armorSet == null)
		{
			return false;
		}
		
		if (!armorSet.containAll(player))
		{
			return false;
		}
		
		// check enchant lvl
		if (chestItem.getEnchantLevel() < ConfigData.ENCHANT_EFFECT_LVL)
		{
			return false;
		}
		
		if (inv.getPaperdollItem(ParpedollType.LEGS).getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
		{
			return false;
		}
		
		if (inv.getPaperdollItem(ParpedollType.HEAD).getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
		{
			return false;
		}
		
		if (inv.getPaperdollItem(ParpedollType.GLOVES).getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
		{
			return false;
		}
		
		if (inv.getPaperdollItem(ParpedollType.FEET).getEnchantLevel() >= ConfigData.ENCHANT_EFFECT_LVL)
		{
			return false;
		}
		
		return true;
	}
}
