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
package main.util;

import java.util.List;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.itemcontainer.inventory.PcInventory;
import net.sf.l2j.gameserver.model.items.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate.StatusUpdateType;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class UtilInventory
{
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public static boolean hasItems(L2PcInstance player, int... itemIds)
	{
		final PcInventory inv = player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemById(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param itemId : ID of the item wanted to be count
	 * @return the quantity of one sort of item hold by the player
	 */
	public static int getItemsCount(L2PcInstance player, int itemId)
	{
		int count = 0;
		
		for (ItemInstance item : player.getInventory().getItems())
		{
			if (item != null && item.getId() == itemId)
			{
				count += item.getCount();
			}
		}
		
		return count;
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 */
	public void giveItems(L2PcInstance player, int itemId, int itemCount)
	{
		giveItems(player, itemId, itemCount, 0);
	}
	
	/**
	 * Give items to the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to add.
	 * @param enchantLevel : Enchant level of items to add.
	 */
	public static void giveItems(L2PcInstance player, int itemId, int itemCount, int enchantLevel)
	{
		// Incorrect amount.
		if (itemCount <= 0)
		{
			return;
		}
		
		// Add items to player's inventory.
		final ItemInstance item = player.getInventory().addItem("Engine", itemId, itemCount, player, player);
		if (item == null)
		{
			return;
		}
		
		// Set enchant level for the item.
		if (enchantLevel > 0)
		{
			item.setEnchantLevel(enchantLevel);
		}
		
		// Send message to the client.
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S1_ADENA);
			smsg.addNumber(itemCount);
			player.sendPacket(smsg);
		}
		else
		{
			if (itemCount > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S2_S1_S);
				smsg.addItemName(itemId);
				smsg.addNumber(itemCount);
				player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM_S1);
				smsg.addItemName(itemId);
				player.sendPacket(smsg);
			}
		}
		
		// Send status update packet.
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdateType.CUR_LOAD, player.getInventory().getTotalWeight());
		player.sendPacket(su);
	}
	
	/**
	 * Remove items from the player's inventory.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of items to destroy.
	 */
	public static void takeItems(L2PcInstance player, int itemId, int itemCount)
	{
		// Find item in player's inventory.
		final ItemInstance item = player.getInventory().getItemById(itemId);
		if (item == null)
		{
			return;
		}
		
		// Tests on count value and set correct value if necessary.
		if (itemCount < 0 || itemCount > item.getCount())
		{
			itemCount = item.getCount();
		}
		
		// Disarm item, if equipped.
		if (item.isEquipped())
		{
			List<ItemInstance> unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		
		// Destroy the quantity of items wanted.
		player.getInventory().destroyItemByItemId("Quest", itemId, itemCount, player, true);
	}
}
